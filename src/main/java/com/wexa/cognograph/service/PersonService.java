package com.wexa.cognograph.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.wexa.cognograph.repository.PersonRepository;

@Service
public class PersonService {
    private final PersonRepository repository;
    public PersonService(PersonRepository repository) { this.repository = repository; }
    public Map<String,Object> person(long id) { return repository.findPerson(id); }
    public List<Map<String,Object>> friends(long id) { return repository.directFriends(id); }
    public List<Map<String,Object>> friendsOfFriends(long id) { return repository.friendsOfFriends(id); }
    public List<Map<String,Object>> network(long id) { return repository.network(id); }
}
