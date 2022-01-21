package com.beyou.admin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AmazonS3UtilTests {
    
    @Test
    public void testListFolder(){
        String folderName = "test-upload";
        List<String> listKeys = AmazonS3Util.listFolder(folderName);
        listKeys.forEach(System.out :: println);
    }

    @Test
    public void testFileUploadFile() throws FileNotFoundException{
        String folderName = "test-upload";
        String fileName = "OIC.docx";
        String filePath = "D:\\conduent\\" + fileName;

        InputStream inputStream = new FileInputStream(filePath);

        AmazonS3Util.uploadFile(folderName, fileName, inputStream);

    }

    @Test
    public void testDeleteFile(){
        String fileName = "test-upload/OIC.docx";
        AmazonS3Util.deleteFile(fileName);
    }

    @Test
    public void testRemoveFolder(){
        String folderName = "test-upload";
        AmazonS3Util.removeFolder(folderName);
    }

}
