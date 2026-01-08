package com.se.backend.service;
import com.se.backend.entity.ScenarioTemplate;
import com.se.backend.mapper.ScenarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScenarioService {
    @Autowired private ScenarioMapper scenarioMapper;
    public List<ScenarioTemplate> getAll() { return scenarioMapper.selectList(null); }
}
