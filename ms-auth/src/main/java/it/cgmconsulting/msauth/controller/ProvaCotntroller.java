package it.cgmconsulting.msauth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProvaCotntroller {

    @GetMapping //localhost:{port}/ms-auth
    public String prova(){
        return "ciao belli!";
    }

    @GetMapping ("/v1")
    public String prova1(){
        return "ciao amministratori!";
    }

    @GetMapping("/v2")
    public String prova2(@RequestHeader("username") String username){
        return "ciao Scrittore " + username + "!";
    }

    @GetMapping ("/v3")
    public String prova3(){
        return "ciao Lettori!";
    }

}
