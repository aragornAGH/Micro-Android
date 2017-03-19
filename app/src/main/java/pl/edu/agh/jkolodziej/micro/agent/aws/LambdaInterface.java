package pl.edu.agh.jkolodziej.micro.agent.aws;


import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

import pl.edu.agh.jkolodziej.micro.agent.intents.AddIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddingFromFileIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ConvertPngToPDFIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;

/**
 * @author - Jakub Kołodziej
 *         AWSLambda Interface to link AWS Lambda fucntion with project code
 *         Each method annotated by <code>@LambdaFunction</code> is separated
 *         AWS Lambda function which is deployed on AWS environment
 */

public interface LambdaInterface {


    @LambdaFunction(functionName = "add-micro-agent")
    AddIntent handleRequest(AddIntent intent);

    @LambdaFunction(functionName = "adding")
    AddingFromFileIntent handleRequest(AddingFromFileIntent intent);

    @LambdaFunction(functionName = "convertPNGToPDF")
    ConvertPngToPDFIntent handleRequest(ConvertPngToPDFIntent intent);

    @LambdaFunction(functionName = "OCR")
    OCRIntent handleRequest(OCRIntent intent);
}

