package org.zerock.myapp.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.zerock.myapp.listener.CommonEntityLifecyleListener;

import lombok.Data;
import lombok.ToString;


@Data

@EntityListeners(CommonEntityLifecyleListener.class)

@Entity(name = "Product4")
@Table(name="product4")
public class Product4
	implements Serializable {	// T1, Many (M)
	@Serial private static final long serialVersionUID = 1L;

	// 1. Set PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long id;			// PK
	
	
	// 2. 일반속성 선언
	@Basic(optional = false)	// Not Null Constraint
	private String name;
	
	
	// 3. 연관관계 매핑
	   
	// 나의 관계대응수가 Many 이기 때문에, 이에 대응되는 Children을
	// 가져야 올바른 정보조회가 가능해집니다.
	
	@OneToMany(
		// 연관관계의 주인을 설정하는 속성:
		// (1) 현재의 엔티티에 대한 FK속성을 가지는 대응 엔티티가 연관관계의 주인
		// (2) 지정될 갑을 (1)에서 밝혀진 엔티티에 선언된 FK속성의 이름을 적는다!
		mappedBy = "productFK",
		
		// 실제 연관관계의 주인이 되는 엔티티의 타입정보를 Clazz 객체로 제공
		targetEntity = Orders.class	
	)
	
	@ToString.Exclude
	private List<Orders> myOrders = new Vector<>();
	
	
	
} // end class
