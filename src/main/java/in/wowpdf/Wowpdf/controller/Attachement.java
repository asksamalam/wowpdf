package in.wowpdf.Wowpdf.controller;

import in.wowpdf.Wowpdf.facade.CompressPdfFacade;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/home/attachment")
public class Attachement {

    @Autowired
    CompressPdfFacade facade;

    @GetMapping("/test")
    public String test(){
        return "Test is working";
    }

    @PostMapping("/resize")
    public ResponseEntity<byte[]> resizePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("quality") float quality
    ){
        return facade.compressPdf(file,quality);
    }

}
