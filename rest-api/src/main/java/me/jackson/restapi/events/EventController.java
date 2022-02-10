package me.jackson.restapi.events;

import lombok.RequiredArgsConstructor;
import me.jackson.restapi.common.ErrorResource;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final Environment environment;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) { // 받기로한 값들만 받아옴
        // EventDto 에 @Valid가 붙게 되면 parameter 값들이 들어올 때 검증을 진행하고
        // 검증에서 에러가 발생할 경우 Errors 객체에 넣어준다
        // Errors 객체는 BeanSerializer로 Serialization이 되지 않는다 (자바 빈 스펙을 따르지 않음)
        // ModelMapper는 리플렉션을 사용하기 때문에 조금 성능은 직접 할당해주는 것보다 느릴 수 있다
        if(errors.hasErrors()){
            return badRequest(errors);
        }
        eventValidator.validate(eventDto, errors);

        if (errors.hasErrors()){
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        Event newEvent = eventRepository.save(event);
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));
        return ResponseEntity.created(createdUri).body(eventResource);
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler){
        Page<Event> page = eventRepository.findAll(pageable);
        var pagedResources = assembler.toResource(page, EventResource::new);
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id){
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity putEvent(@PathVariable Integer id,
                                   @RequestBody @Valid EventDto eventDto,
                                   Errors errors){
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(optionalEvent.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        if(errors.hasErrors()){
            return badRequest(errors);
        }

        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()){
            return badRequest(errors);
        }

        Event existingEvent = optionalEvent.get();
        modelMapper.map(eventDto, existingEvent);
        Event savedEvent = eventRepository.save(existingEvent);

        EventResource eventResource = new EventResource(savedEvent);
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

    private ResponseEntity<ErrorResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorResource(errors));
    }

}
