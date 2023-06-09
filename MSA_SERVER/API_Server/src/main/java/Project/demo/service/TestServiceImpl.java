package Project.demo.service;

import Project.demo.DTO.TestDTO;
import Project.demo.mapper.TestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestMapper testMapper;

    @Override
    public List<TestDTO> getUserList() {
        return testMapper.getUserList();
    }

    @Override
    public List<TestDTO> SearchNumber(String phone){
        return testMapper.SearchNumber(phone);
    }

    @Override
    public List<TestDTO> InsertReport(String phone) {
        return testMapper.InsertReport(phone);
    }


}
