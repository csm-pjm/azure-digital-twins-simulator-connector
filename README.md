# Azure Digital Twins connector
The aim of this project is to :
 - read data from an ADT
 - flatten these data in a list of CSVData Pojo defined in the library simulator-connector-commons
 - Export Csv Files containing these data

## Properties to overwrite :
Here is the list of properties that should be changed (in ```META-INF/microprofile-config.properties``` file):
- azure.client.id
- azure.tenant.id
- azure.client.secret
- azure.digital.twins.url
- export.csv.file.absolute.path

If you want to overwrite these properties, you can write your own property values in the ```META-INF/microprofile-config.properties``` file, or set a property's system, or an environment variable named :
- AZURE_CLIENT_ID : the Azure client id (can be found under the App registration screen)
- AZURE_TENANT_ID : the Azure Tenant id (can be found under the App registration screen)
- AZURE_CLIENT_SECRET : the app client secret (can be found under the App registration/certificates and secrets screen)
- AZURE_DIGITAL_TWINS_URL : the url of the ADT targeted (can be found in the specific resource screen)
- EXPORT_CSV_FILE_ABSOLUTE_PATH : the absolute path to export all csv files (don't forget the / at the end)

##Change the default container registry

```
  <to>
    <image><your_container_registry>/azure-digital-twins-simulator-connector</image>
  </to>
```
See [Jib project Configuration]("https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin#configuration") to set correctly your container registry (GCR, ECR, ACR, Docker Hub Registry)

Build your container image with:

```shell
mvn compile jib:build
```

Subsequent builds are much faster than the initial build.

#### Build to Docker daemon

Jib can also build your image directly to a Docker daemon. This uses the `docker` command line tool and requires that you have `docker` available on your `PATH`.

```shell
mvn compile jib:dockerBuild
```

For more information, see [Jib project Build]("https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin#build-your-image")

#### How to run your image locally 

```
docker run \ 
-v <<local_export_dir_path>>:/tmp \ 
-e EXPORT_CSV_FILE_ABSOLUTE_PATH=/tmp/ \ 
-e ADT_INSTANCE_URL=https://XXX.XXX.XXX.digitaltwins.azure.net \
-e AZURE_TENANT_ID=<<azure_tenant_id>> \
-e AZURE_CLIENT_ID=<<azure_client_id>> \
-e AZURE_CLIENT_SECRET=<<azure_client_secret>> \
<your_container_registry>/azure-digital-twins-simulator-connector
```

You can find all export files under the directory "local_export_dir_path" specified above


## POM.xml dependency 

```
<dependency>
  <groupId>com.cosmotech</groupId>
  <artifactId>azure-digital-twins-simulator-connector</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>

```
or 
```
    <dependency>
      <groupId>com.github.Cosmo-Tech</groupId>
      <artifactId>azure-digital-twins-simulator-connector</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
```

