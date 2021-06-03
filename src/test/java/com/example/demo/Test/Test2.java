package com.example.demo.Test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test2 {

    @Test
    public void contextLoads() throws UnsupportedEncodingException {
        String s = new String(Base64.getDecoder().decode("amF2YWJveToxNjIzOTAxMzExNDM4OmY1YzQwZTIyZTI5NTQ1OGNmNThmODJhMjI2YTMzODJl"),"UTF-8");
        System.out.println("s= " + s);
    }
}
