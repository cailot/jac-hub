package hyung.jin.seo.jae.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.ForeignKey;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.CascadeType;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Invoice")
public class Invoice{ 
	
	@Id
	@GeneratedValue(generator = "invoiceIdGenerator", strategy = GenerationType.IDENTITY)
	@GenericGenerator(name = "invoiceIdGenerator", strategy = "hyung.jin.seo.jae.utils.InvoiceIdGenerator")
 	private Long id;
    
	@OneToMany(mappedBy = "invoice", cascade = {
     	CascadeType.PERSIST,
		CascadeType.MERGE,
		CascadeType.REFRESH,
		CascadeType.DETACH
	})
    private Set<Enrolment> enrolments = new HashSet<>();

	public void addEnrolment(Enrolment enrolment){
		enrolments.add(enrolment);
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = {
		CascadeType.PERSIST,
		CascadeType.MERGE,
		CascadeType.REFRESH,
		CascadeType.DETACH
	})
	@JoinColumn(name = "invoiceId", foreignKey = @ForeignKey(name = "FK_Invoice_Payment"))
	private Set<Payment> payments = new LinkedHashSet<>();

	public void addPayment(Payment payment){
		payments.add(payment);
	}

	// @OneToMany(fetch = FetchType.LAZY, cascade = {
	// 	CascadeType.PERSIST,
	// 	CascadeType.MERGE,
	// 	CascadeType.REFRESH,
	// 	CascadeType.DETACH
	// })
	// @JoinColumn(name = "invoiceId", foreignKey = @ForeignKey(name = "FK_Invoice_Outstanding"))
	// private Set<Outstanding> outstandings = new LinkedHashSet<>();

	// public void addOutstanding(Outstanding stand){
	// 	outstandings.add(stand);
	// }

	@OneToMany(mappedBy = "invoice", cascade = {
     	CascadeType.PERSIST,
		CascadeType.MERGE,
		CascadeType.REFRESH,
		CascadeType.DETACH
	})
    private Set<Material> materials = new HashSet<>();

	public void addMaterial(Material material){
		materials.add(material);
	}

	// auto update to current date
	@CreationTimestamp
    private LocalDate registerDate;

	@CreatedDate
    private LocalDate paymentDate;

	@Column
    private int credit;

	@Column
    private double discount;

	@Column
    private double amount;

	@Column
	private double paidAmount;

	@Column(length = 100)
    private String info;

	@Transient // not persistent to DB
	@JsonIgnore // ignore serialisation/deserialisation via Json
	private Long studentId;	

}
