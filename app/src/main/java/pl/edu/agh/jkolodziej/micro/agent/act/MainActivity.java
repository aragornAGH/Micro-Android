package pl.edu.agh.jkolodziej.micro.agent.act;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.google.common.collect.Lists;

import org.nzdis.micro.bootloader.MicroBootProperties;
import org.nzdis.micro.messaging.MTRuntime;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.agent.R;
import pl.edu.agh.jkolodziej.micro.agent.aws.AWSLambdaRequester;
import pl.edu.agh.jkolodziej.micro.agent.aws.LambdaInterface;
import pl.edu.agh.jkolodziej.micro.agent.enums.Action;
import pl.edu.agh.jkolodziej.micro.agent.enums.IntentType;
import pl.edu.agh.jkolodziej.micro.agent.helpers.AndroidFilesSaverHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.OCRHelper;
import pl.edu.agh.jkolodziej.micro.agent.service.ExampleService;
import pl.edu.agh.jkolodziej.micro.agent.service.TestAgentService;
import pl.edu.agh.mm.energy.PowerTutorFacade;

public class MainActivity extends AppCompatActivity {

    private ResponseFromServiceReceiver receiver;
    public static List<String> results = Lists.newArrayList();
    public static ArrayAdapter<String> adapter;
    private boolean providerRun;
    private boolean providerAWSRun;
    CognitoCachingCredentialsProvider credentialsProvider;
    LambdaInvokerFactory factory;
    LambdaInterface myInterface;

    private EditText sub1Text;
    private EditText sub2Text;

    private EditText sub1Label;
    private EditText sub2Label;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (shouldAskPermissions()) {
            verifyStoragePermissions(this);
        }

        AndroidFilesSaverHelper.INTERNAL_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();
        OCRHelper.copyTessData();


        IntentFilter filter = new IntentFilter(ResponseFromServiceReceiver.RESPONSE);
        receiver = new ResponseFromServiceReceiver(MainActivity.this);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        sub1Text = (EditText) findViewById(R.id.subOneText);
        sub2Text = (EditText) findViewById(R.id.subTwoText);

        sub1Label = (EditText) findViewById(R.id.subOneLabelText);
        sub2Label = (EditText) findViewById(R.id.subTwoLabelText);

        final Spinner spinner = (Spinner) findViewById(R.id.taskTypeSpinner);
        ArrayAdapter<IntentType> adapter = new ArrayAdapter<IntentType>(this, android.R.layout.simple_spinner_item, IntentType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IntentType intentType = (IntentType) spinner.getSelectedItem();
                boolean hide = IntentType.ADDING != intentType;
                sub1Text.setVisibility(hide ? View.GONE : View.VISIBLE);
                sub2Text.setVisibility(hide ? View.GONE : View.VISIBLE);
                sub1Label.setVisibility(hide ? View.GONE : View.VISIBLE);
                sub2Label.setVisibility(hide ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button providerButton = (Button) findViewById(R.id.mobileProviderButton);
        providerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (providerRun) {
                    Toast.makeText(getApplicationContext(), "Provider jest już uruchomiony ;-)", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ExampleService.class);
                    intent.putExtra("action", Action.RUN_PROVIDER);
                    startService(intent);
                }
            }

        });

        Button AWSButton = (Button) findViewById(R.id.AWSProviderButton);
        AWSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (providerAWSRun) {
                    Toast.makeText(getApplicationContext(), "Provider AWS jest już uruchomiony ;-)", Toast.LENGTH_SHORT).show();
                }
                if (!providerRun) {
                    Toast.makeText(getApplicationContext(), "Najpierw uruchom Android Providera", Toast.LENGTH_LONG).show();
                } else {
                    // Create an instance of CognitoCachingCredentialsProvider
                    credentialsProvider = new CognitoCachingCredentialsProvider(
                            getApplicationContext(),
                            MTRuntime.aws_identity_pool,
                            Regions.valueOf(MTRuntime.aws_region));

                    ClientConfiguration cc = new ClientConfiguration();
                    cc.setSocketTimeout(0);

                    // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
                    factory = new LambdaInvokerFactory(
                            getApplicationContext(),
                            Regions.valueOf(MTRuntime.aws_region),
                            credentialsProvider, cc);

                    // Create the Lambda proxy object with default Json data binder.
                    // You can provide your own data binder by implementing
                    // LambdaDataBinder
                    myInterface = factory.build(LambdaInterface.class);

                    Intent intent = new Intent(MainActivity.this, ExampleService.class);
                    intent.putExtra("action", Action.RUN_AWS_PROVIDER);
                    startService(intent);
                }
            }
        });

        Button runButton = (Button) findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentType intentType = (IntentType) spinner.getSelectedItem();
                Intent intent = new Intent(MainActivity.this, ExampleService.class);
                intent.putExtra("action", Action.RUN_CLIENT);
                intent.putExtra("intentType", intentType);
                if (IntentType.ADDING == intentType) {
                    intent.putExtra("sub1", Double.parseDouble(sub1Text.getText().toString().replaceAll(",", ".")));
                    intent.putExtra("sub2", Double.parseDouble(sub2Text.getText().toString().replaceAll(",", ".")));
                }
                startService(intent);

            }
        });

        Button testButton = (Button) findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestAgentService.class);
                startService(intent);
            }
        });

        try {
            MicroBootProperties.readConfiguration();
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Fail during reading properties");
        }

        PowerTutorFacade powerTutorFacade = PowerTutorFacade.getInstance(this, "energy");
        powerTutorFacade.startPowerTutor();
        PowerTutorFacade.getInstance(this, "energy").bindService();
    }

    public class ResponseFromServiceReceiver extends BroadcastReceiver {

        public static final String RESPONSE = "pl.edu.agh.jkolodziej.micro.agent.RESPONSE";
        private final Context mContext;

        public ResponseFromServiceReceiver(Context context) {
            this.mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String worker = intent.getStringExtra("worker");
            String result = intent.getStringExtra("result");
            Long nanoSeconds = intent.getLongExtra("duration", 0L);
            Double batteryPercentage = intent.getDoubleExtra("batteryPercentage", 0.0);
            IntentType intentType = (IntentType) intent.getSerializableExtra("intentType");
            Boolean provRun = intent.getBooleanExtra("provider_run", false);
            Boolean provAWSRun = intent.getBooleanExtra("provider_aws_run", false);

            // AWSLambda
            if (!AWSLambdaRequester.requestIfNeeded(intent, myInterface, mContext, (ListView) findViewById(R.id.listView))) {
                if (provRun) {
                    Toast.makeText(mContext, "Provider wystartował ;-)", Toast.LENGTH_SHORT).show();
                    providerRun = true;
                } else if (provAWSRun) {
                    Toast.makeText(mContext, "Provider AWS wystartował ;-)", Toast.LENGTH_SHORT).show();
                    providerAWSRun = true;
                } else {
                    Toast.makeText(mContext, intentType + " - " + worker + " - " + result + " " + (nanoSeconds / Math.pow(10.0, 6)) + "ms; battery " +
                            batteryPercentage + "%", Toast.LENGTH_SHORT).show();
                    results.add(intentType + " - " + worker + " - " + (nanoSeconds / Math.pow(10.0, 6)) + "ms; battery: " + batteryPercentage + "%");
                    ListView list = (ListView) findViewById(R.id.listView);
                    adapter = new ArrayAdapter<String>(mContext, R.layout.row_list_view, results);
                    list.setAdapter(adapter);
                    list.refreshDrawableState();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
