package ua.dymohlo.PetMusicStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ua.dymohlo.PetMusicStorage.entity.MusicFile;
import ua.dymohlo.PetMusicStorage.service.MusicFileService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/optimal_subscription")
public class OptimalSubscriptionPageController {
    private final MusicFileService musicFileService;

    @GetMapping
    public ModelAndView showOptimalSubscriptionPage() {
        log.info("Optimal subscription page accessed.");
        return new ModelAndView("pages/optimal_subscription_music_page");
    }

    @GetMapping("/list_optimal_subscription")
    public ResponseEntity<Resource> listOptimalSubscription(@RequestParam("subscriptionName") String subscriptionName) {
        try {
            List<MusicFile> musicFiles = musicFileService.findMusicFileBySubscription(subscriptionName);
            if (musicFiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(baos);
            for (MusicFile musicFile : musicFiles) {
                Path filePath = Path.of(musicFile.getFilePath());
                ZipEntry zipEntry = new ZipEntry(filePath.getFileName().toString());
                zipOut.putNextEntry(zipEntry);
                Files.copy(filePath, zipOut);
                zipOut.closeEntry();
            }
            zipOut.close();
            ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"music_files.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
            log.error("Error creating ZIP file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}