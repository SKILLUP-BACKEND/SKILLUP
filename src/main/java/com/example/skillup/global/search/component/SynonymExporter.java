package com.example.skillup.global.search.component;


import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.enums.SynonymStatus;
import com.example.skillup.global.search.exception.SearchException;
import com.example.skillup.global.search.repository.SynonymGroupRepository;
import com.example.skillup.global.search.repository.SynonymTermRepository;
import com.example.skillup.global.exception.CommonErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SynonymExporter {

    private final SynonymGroupRepository synonymGroupRepository;
    private final SynonymTermRepository synonymTermRepository;

    @Value("${skillup.synonyms.export-dir}") private String exportDir;
    @Value("${skillup.synonyms.file-name}") private String fileName;

    @Transactional(readOnly = true)
    public Path exportActiveToFile(String locale) {
        List<SynonymGroup> groups = synonymGroupRepository.findSynonymGroupByStatusAndLocale(SynonymStatus.ACTIVE , locale);
        Map<Long, List<String>> termsByGroup = synonymTermRepository.findAllActiveGrouped(locale);

        List<String> lines = new ArrayList<>();
        for (SynonymGroup g : groups) {
            List<String> terms = termsByGroup.getOrDefault(g.getId(), List.of());
            if (!terms.isEmpty()) {
                lines.add(String.join(", ", terms));
            }
        }

        Path path = Paths.get(exportDir, fileName);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return path;
        } catch (IOException e) {
            throw new SearchException(CommonErrorCode.FILE_UPLOAD_ERROR, "동의어 파일 생성 실패: " + e.getMessage());
        }
    }


}
