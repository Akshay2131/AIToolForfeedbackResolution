package com.example.aitoolforfeedbackresolution.services;

import com.example.aitoolforfeedbackresolution.model.LOG_ERROR;
import com.example.aitoolforfeedbackresolution.repositories.LogErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiService{

    @Autowired
    LogErrorRepository logErrorRepository;

    public String getData(String queryToRun){

        List<LOG_ERROR> data = logErrorRepository.findRelevantData();
    }

}
