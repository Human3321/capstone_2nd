package Project.demo.service;

import Project.demo.DTO.TestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface TestService {
    public List<TestDTO> getUserList();
    public List<TestDTO> SearchNumber(String phone);
    public List<TestDTO> InsertReport(String phone);


}
