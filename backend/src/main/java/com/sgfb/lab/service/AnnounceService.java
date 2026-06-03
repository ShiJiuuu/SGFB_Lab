package com.sgfb.lab.service;

import com.sgfb.lab.mapper.AnnounceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnounceService {

    @Autowired
    private AnnounceMapper announceMapper;

    public String getAnnounce() {
        return announceMapper.getAnnounce();
    }

    public boolean updateAnnounce(String announce) {
        return announceMapper.updateAnnounce(announce) > 0;
    }
}
