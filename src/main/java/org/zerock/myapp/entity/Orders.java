package org.zerock.myapp.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.zerock.myapp.listener.CommonEntityLifecyleListener;

import lombok.Data;


@Data

@EntityListeners(CommonEntityLifecyleListener.class)

@Entity(name = "Orders")
@Table(name="orders")
public class Orders
	implements Serializable {	// Cross Entity with 인조키(PK)
	@Serial private static final long serialVersionUID = 1L;

	// 1. Set PK (인조키로 선언하자!!..단순이 최고!)
	//    이때 Artificial Key를 "Surrogate Key"라고도 부릅니다.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Long id;			// PK
	
		
	// 2. 일반속성 선언 - T1 FK, T2 FK
	
	// =======================
	// Set T1 FK (M:1)
	// =======================
	@ManyToOne(targetEntity = Shopper4.class)	// FK의 관계가 어떤 유형인지 설정
	@JoinColumn(name = "shopper_id")	// 진짜 JPA Provider에게 이 속성이 FK임을 선언
	private Shopper4 shopperFK; // FK속성명은 유의미하게만 선언하시면 됩니다.

	
	// =======================
	// Set T2 FK (N:1)
	// =======================
	@ManyToOne(targetEntity = Product4.class)	// FK의 관계가 어떤 유형인지 설정
	@JoinColumn(name = "product_id")	// 진짜 JPA Provider에게 이 속성이 FK임을 선언
	private Product4 productFK; // FK속성명은 유의미하게만 선언하시면 됩니다.
	
	
	// 관계타입에서 추출된 추가 속성 선언
	@Basic(optional = false) private int orderAmount;		// 주문수량
	@Basic(optional = false) private long orderPrice;		// 주문가격
		
	@CreationTimestamp			// 자동으로 주문일시 생성
	@Basic(optional = false)
	private Date orderDate;		// 주문일시
	
	
	// 3. 연관관계 매핑


   
} // end class


