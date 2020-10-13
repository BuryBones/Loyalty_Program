import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table (name = "client")
public class Client {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column (name = "phone")
    private String phone;

    @Column (name = "name")
    private String name;

    @Column (name = "points")
    private Integer points;

    // TODO: Do I really need the following fields? May I not have them?

    @Column (name = "reg_date")
    private Date registrationDate;

    @Column (name = "last_add_date")
    private Timestamp lastAddDate;

    @Column (name = "last_use_date")
    private Timestamp lastUseDate;

    @Column (name = "total_spent")
    private Double totalSpent;

    @Column (name = "total_used")
    private Integer totalUsed;

    // TODO: which constructors do I need with Hibernate?
    public Client() {

    }
    public Client(String phone, String name) {
        this.phone = phone;
        this.name = name;
        // TODO: check if Postgres set defaults itself
        points = 0;
        // current date
        registrationDate = new Date();
        totalSpent = 0.0;
        totalUsed = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getLastAddDate() {
        return lastAddDate;
    }

    public void setLastAddDate(Timestamp lastAddDate) {
        this.lastAddDate = lastAddDate;
    }

    public Date getLastUseDate() {
        return lastUseDate;
    }

    public void setLastUseDate(Timestamp lastUseDate) {
        this.lastUseDate = lastUseDate;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void increaseTotalSpent(float add) {
        totalSpent += add;
    }

    public Integer getTotalUsed() {
        return totalUsed;
    }

    public void increaseTotalUsed(Integer add) {
        totalUsed += add;
    }

    // for development usage
    public String toString() {
        return String.format("%s %d %s %d%nReg: %tF%nLastAdd: %tc%nLastUse: %tc%nTot.Spent: %f%nTot.Used: %d%n",
                name,id,phone,points,registrationDate,lastAddDate,lastUseDate,totalSpent,totalUsed);
    }
}
