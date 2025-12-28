package com.mopl.domain.service.tag;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagModel> findOrCreateTags(List<String> tags) {
        List<TagModel> tagModels = new ArrayList<>();

        tags.forEach(tag -> {
            TagModel tagModel = tagRepository.findByName(tag.strip())
                .orElseGet(() -> tagRepository.save(TagModel.create(tag)));

            tagModels.add(tagModel);
        });

        return tagModels;
    }
}
