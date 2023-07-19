package it.cgmconsulting.mspost.controller;

import it.cgmconsulting.mspost.service.PostService;
import it.cgmconsulting.mspost.service.XlsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class XlsController {

    private final PostService postService;
    private final XlsService xlsService;

    @GetMapping("v1/get-report")
    public ResponseEntity<?> getPdfFromPost(){
        InputStream xlsFile = null;
        ResponseEntity<InputStreamResource> response = null;

        try{
            xlsFile = xlsService.createReport();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Access-Controll-Allow-Origin", "*"); // Chiunque lo può generare il pdf
            httpHeaders.add("Access-Controll-Allow-Method", "GET");
            httpHeaders.add("Access-Controll-Allow-Header", "Content-Type");
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.ms-excel")); // Il mediatype si trova nelle codifiche dei siti di chi li produce
            httpHeaders.add("Content-Disposition", "attachment; filename=report.xls"); //con attachment parte un download

            response = new ResponseEntity<InputStreamResource>(
                    new InputStreamResource(xlsFile),
                    httpHeaders,
                    HttpStatus.OK
            );

        } catch (Exception e){
            response = new ResponseEntity<InputStreamResource>(
                    new InputStreamResource(null, "\"Something went wrong creating the pdf...\""),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }


        return response;
    }

}
