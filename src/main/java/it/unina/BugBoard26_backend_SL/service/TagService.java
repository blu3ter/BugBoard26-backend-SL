package it.unina.BugBoard26_backend_SL.service;

import it.unina.BugBoard26_backend_SL.dto.TagDto;
import it.unina.BugBoard26_backend_SL.entity.Tag;
import it.unina.BugBoard26_backend_SL.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    // --- Entity-oriented API (kept for compatibility) ---

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }

    public Optional<Tag> findByName(String name) {
        return tagRepository.findByName(name);
    }

    @Transactional
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }

    // --- DTO-oriented API used by Controller (centralizza mapping) ---

    public List<TagDto> findAllDto() {
        return findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public TagDto findByIdDto(Long id) {
        Tag t = findById(id).orElseThrow(NoSuchElementException::new);
        return toDto(t);
    }

    @Transactional
    public TagDto create(TagDto dto) {
        Tag t = fromDto(dto);
        Tag saved = tagRepository.save(t);
        return toDto(saved);
    }

    @Transactional
    public TagDto update(Long id, TagDto dto) {
        Tag existing = tagRepository.findById(id).orElseThrow(NoSuchElementException::new);
        existing.setName(dto.getName());
        existing.setColour(dto.getColour());
        Tag saved = tagRepository.save(existing);
        return toDto(saved);
    }

    // Mapper: Entity -> DTO
    private TagDto toDto(Tag t) {
        TagDto d = new TagDto();
        d.setId(t.getId());
        d.setName(t.getName());
        d.setColour(t.getColour());
        return d;
    }

    // Mapper: DTO -> Entity
    private Tag fromDto(TagDto dto) {
        Tag t = new Tag();
        t.setId(dto.getId());
        t.setName(dto.getName());
        t.setColour(dto.getColour());
        return t;
    }
}
