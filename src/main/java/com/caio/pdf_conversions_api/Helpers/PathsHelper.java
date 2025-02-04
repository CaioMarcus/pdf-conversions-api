package ConversoesAPI.Conversions.Helpers;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Classe de Gerenciamento de diretórios.
 */
@Data
@Configuration
public class PathsHelper {
    @Getter
    private static String conversionsBasePath_STATIC;
    @Value("${conversions_path}")
    public void setConversionsBasePath_STATIC(String nonStaticConversionsBasePath){
        PathsHelper.conversionsBasePath_STATIC = nonStaticConversionsBasePath;
    }


    /**
     * Gera um novo caminho baseado no nome da conversão. É importante para não ter conflitos no caso de conversões de mesmo nome.
     *
     * @param conversionName the conversion name
     * @return the string
     * @throws IOException the io exception
     */
    public static String GenerateConversionPath(String conversionName) throws IOException {

        String conversionId = createConversionId(conversionName);

        if (ConversionExists(conversionId))
            return GenerateConversionPath(conversionName);

        createConversionsFolders(conversionId);
        return conversionId;
    }

    /**
     * Cria um novo id de conversão baseado no nome da mesma.
     *
     * @param conversionName the conversion name
     * @return the string
     */
    public static String createConversionId(String conversionName){
        return conversionName + LocalDateTime.now().toString().replace(":", "")
                .replace("-", "")
                .replace(".", "");
    }

    /**
     * Cria a string do caminho da converão, baseado em seu id.
     *
     * @param conversionId the conversion id
     * @return the string
     */
    public static String GetConversionPath(String conversionId){
        return conversionsBasePath_STATIC + File.separator + conversionId + File.separator;
    }


    /**
     * Verifica se a conversão já existe. Importante para não sobrescrever, ou utilizar arquivos de outra conversão
     *
     * @param conversionId the conversion id
     * @return the boolean
     */
    public static boolean ConversionExists(String conversionId){
        File dir = new File(conversionsBasePath_STATIC + File.separator + conversionId + File.separator);
        return dir.exists();
    }

//    public static boolean ConversionHasFiles(String conversionId){
//        File dir = new File(conversionsBasePath_STATIC + File.separator + conversionId);
//        if (!dir.exists()) return false;
//        try {
//            if (dir.listFiles().length == 0) return false;
//        } catch (NullPointerException e){
//            return false;
//        }
//        return true;
//    }

//    public static void DeleteConversionFolders(String conversionId){
//        String path = GetConversionPath(conversionId);
//        FileSystemUtils.deleteRecursively(new File(path));
//    }

    //region Helpers

    /**
     * Chama o sistema para realizar a criaćão do diretório da conversão.
     * @param conversionId
     * @throws IOException
     */

    private static void createConversionsFolders(String conversionId) throws IOException {
        String path = conversionsBasePath_STATIC + File.separator + conversionId + File.separator;
        Files.createDirectory(Path.of(path));
    }
    //region
}
