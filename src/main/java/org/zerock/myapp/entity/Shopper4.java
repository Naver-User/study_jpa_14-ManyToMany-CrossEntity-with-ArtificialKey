package org.zerock.myapp.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import lombok.extern.slf4j.Slf4j;


//@Log4j2
@Slf4j

@Data

@EntityListeners(CommonEntityLifecyleListener.class)

@Entity(name = "Shopper4")
@Table(name="shopper4")
public class Shopper4
	implements Serializable {	// T1, Many (N)
	@Serial private static final long serialVersionUID = 1L;

	// 1. Set PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shopper_id")
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
		mappedBy = "shopperFK",
		
		// 실제 연관관계의 주인이 되는 엔티티의 타입정보를 Clazz 객체로 제공
		targetEntity = Orders.class	
	)
	
	@ToString.Exclude
	private List<Orders> myOrders = new Vector<>();

	
	// 바로 위의 List<Orders>에 신규로 추가될 주문내역에 대한, 유효성 검증 메소드입니다.
	public boolean order(Orders newOrder) {
		log.trace("order({}) invoked.", newOrder);
		
		// 유효성 검사해서 통과된 경우에만, List Collection에 유효한 주문으로 추가
		if(Objects.nonNull(newOrder.getProductFK())) return false;
		if(newOrder.getOrderAmount() < 1) return false;
		if(newOrder.getOrderPrice()  < 1) return false;
		
		// 더이상 EntityManager를 통해서, 신규 주문내역이 저장되는것이 아니기 때문에
		// 아래와 같이 주문일시도 직접 생성해서 넣어주셔야 합니다.
		newOrder.setOrderDate(new Date());		
		
		return this.myOrders.add(newOrder);
	} // order

   
} // end class


