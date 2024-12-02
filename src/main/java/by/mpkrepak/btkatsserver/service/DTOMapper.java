package by.mpkrepak.btkatsserver.service;

import by.mpkrepak.btkatsserver.domain.Record;
import by.mpkrepak.btkatsserver.dto.DataDTO;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class DTOMapper {
    public List<DataDTO> mapToDataDTO(List<Record> records) {
        List<DataDTO> dtos = new ArrayList<DataDTO>();

        records.forEach(record -> {
            Date date = new Date(record.getRecordDate());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formattedDate = sdf.format(date);

            var totalSeconds = record.getDuration();
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            DataDTO dto = new DataDTO();
            dto.setDate(formattedDate);
            dto.setDuration(formattedTime);
            dto.setId(record.getId());
            dto.setCaller(record.getCaller());
            dto.setReceiver(record.getCallReceiver());
            int index = record.getPath().indexOf('/');
            String file = record.getPath().substring(index + 1);
            dto.setFilePath(file);

            dtos.add(dto);
        });

        return dtos;
    }
}
