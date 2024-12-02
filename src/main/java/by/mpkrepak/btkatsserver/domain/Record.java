package by.mpkrepak.btkatsserver.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "records")
@Data
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String pbxId;
    private String name;
    private String path;
    private long recordDate;
    private String caller;
    private String callReceiver;
    private int duration;
    private boolean checked;

}
