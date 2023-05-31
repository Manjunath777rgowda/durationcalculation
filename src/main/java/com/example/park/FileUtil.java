/* Copyright(C) 2020-21. Nuvepro Pvt. Ltd. All rights reserved */

package com.example.park;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * This util is used perform the file related operations
 */
@Slf4j
public class FileUtil {

    public static File uploadFile( MultipartFile multipartFile ) throws Exception
    {
        if( multipartFile.isEmpty() )
        {
            log.error("File can not be empty");
            throw new Exception("File not found");
        }

        String destinationFile = new File("").getAbsolutePath()+File.separator+multipartFile.getOriginalFilename();
        File file = new File(destinationFile);
        if( file.exists() )
            file.delete();

        multipartFile.transferTo(file);
        return file;
    }

}
