package eu.makdeniss.service.impl;

import eu.makdeniss.service.PersonService;
import eu.makdeniss.domain.Person;
import eu.makdeniss.repository.PersonRepository;
import eu.makdeniss.repository.search.PersonSearchRepository;
import eu.makdeniss.service.dto.PersonDTO;
import eu.makdeniss.service.mapper.PersonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Person.
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService{

    private final Logger log = LoggerFactory.getLogger(PersonServiceImpl.class);
    
    private final PersonRepository personRepository;

    private final PersonMapper personMapper;

    private final PersonSearchRepository personSearchRepository;

    public PersonServiceImpl(PersonRepository personRepository, PersonMapper personMapper, PersonSearchRepository personSearchRepository) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.personSearchRepository = personSearchRepository;
    }

    /**
     * Save a person.
     *
     * @param personDTO the entity to save
     * @return the persisted entity
     */
    @Override
    public PersonDTO save(PersonDTO personDTO) {
        log.debug("Request to save Person : {}", personDTO);
        Person person = personMapper.personDTOToPerson(personDTO);
        person = personRepository.save(person);
        PersonDTO result = personMapper.personToPersonDTO(person);
        personSearchRepository.save(person);
        return result;
    }

    /**
     *  Get all the people.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<PersonDTO> findAll() {
        log.debug("Request to get all People");
        List<PersonDTO> result = personRepository.findAll().stream()
            .map(personMapper::personToPersonDTO)
            .collect(Collectors.toCollection(LinkedList::new));

        return result;
    }

    /**
     *  Get one person by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public PersonDTO findOne(Long id) {
        log.debug("Request to get Person : {}", id);
        Person person = personRepository.findOne(id);
        PersonDTO personDTO = personMapper.personToPersonDTO(person);
        return personDTO;
    }

    /**
     *  Delete the  person by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Person : {}", id);
        personRepository.delete(id);
        personSearchRepository.delete(id);
    }

    /**
     * Search for the person corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<PersonDTO> search(String query) {
        log.debug("Request to search People for query {}", query);
        return StreamSupport
            .stream(personSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .map(personMapper::personToPersonDTO)
            .collect(Collectors.toList());
    }
}
