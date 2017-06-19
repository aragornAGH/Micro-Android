package pl.edu.agh.jkolodziej.micro.agent.aws;


import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

import pl.edu.agh.jkolodziej.micro.agent.intents.AddIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddingFromFileIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.PNGToPDFIntent;

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
    PNGToPDFIntent handleRequest(PNGToPDFIntent intent);

    @LambdaFunction(functionName = "OCR")
    OCRIntent handleRequest(OCRIntent intent);
}

