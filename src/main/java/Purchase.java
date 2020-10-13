import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "purchase")
public class Purchase {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fiscal")
    private String fiscal;

    @Column(name = "amount")
    private float amount;

    @Column(name = "points_change")
    private Integer pointsChange;

    @Column(name = "time_n_date")
    private Timestamp timestamp;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    public Purchase() {
    }
    public Purchase(String fiscal, float amount, int pointsChange, Client client) {
        this.fiscal = fiscal;
        this.amount = amount;
        this.pointsChange = pointsChange;
        this.client = client;
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Integer getId() {
        return id;
    }

    public String getFiscal() {
        return fiscal;
    }

    public void setFiscal(String fiscal) {
        this.fiscal = fiscal;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Integer getPointsChange() {
        return pointsChange;
    }

    public void setPointsChange(Integer pointsChange) {
        this.pointsChange = pointsChange;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    // for development usage
    public String toString() {
        return String.format("Purchase %d %f RUB, %d points %tc%n", id,amount,pointsChange,timestamp);
    }
}
