package by.mpkrepak.btkatsserver.controller;

import by.mpkrepak.btkatsserver.dto.DataDTO;
import by.mpkrepak.btkatsserver.repository.RecordsRepository;
import by.mpkrepak.btkatsserver.service.DTOMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping
@Data
@RequiredArgsConstructor
public class DataController {
    @Value("${settings.download_path}")
    private String downloadPath;
    private final RecordsRepository recordsRepository;
    private final DTOMapper mapper;
    @GetMapping("/index")
    public String index(@RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        var dataPage = recordsRepository.findAll(pageable);
        var data = mapper.mapToDataDTO(dataPage.getContent());

        model.addAttribute("dto", data);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", dataPage.getTotalPages());
        return "index";
    }

    @GetMapping("/play")
    public ResponseEntity<Resource> playAudio(@RequestParam Long id) {
        DataDTO data = mapper.mapToDataDTO(recordsRepository.findById(id).stream().toList()).get(0);
        File file = data.getFile();
        Resource resource = new FileSystemResource(downloadPath + "\\" + file.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    }



}
