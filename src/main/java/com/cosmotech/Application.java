package com.cosmotech;

import com.cosmotech.connector.adt.impl.AzureDigitalTwinsConnector;

public class Application {

  public static void main( String[] args ){

    final AzureDigitalTwinsConnector azureDigitalTwinsConnector = new AzureDigitalTwinsConnector();
    azureDigitalTwinsConnector.process();

  }
}
