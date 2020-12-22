# Environment variables to set:
## Azure Digital Twins
- ADT_INSTANCE_URL : the url of the ADT targeted (can be found in the specific resource screen)
- AZURE_TENANT_ID : the Azure Tenant id (can be found under the App registration screen)
- AZURE_CLIENT_ID : the Azure client id (can be found under the App registration screen)
- AZURE_CLIENT_SECRET : the app client secret (can be found under the App registration/certificates and secrets screen)
##Common
- EXPORT_CSV_FILE_ABSOLUTE_PATH : the absolute path to export all csv files (don't forget the / at the end)

# Build your image

Change the default container registry

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