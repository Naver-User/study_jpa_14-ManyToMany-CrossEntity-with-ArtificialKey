package org.zerock.myapp.association;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.zerock.myapp.entity.Orders;
import org.zerock.myapp.entity.Product4;
import org.zerock.myapp.entity.Shopper4;
import org.zerock.myapp.util.PersistenceUnits;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


//@Log4j2
@Slf4j

@NoArgsConstructor

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class M2MMappingUsingCrossEntityWithArtificalKeyTests {
	private EntityManagerFactory emf;
	private EntityManager em;
	
	
	@BeforeAll
	void beforeAll() {	// 1회성 전처리
		log.trace("beforeAll() invoked.");
		
		// -- 1 ------------
		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.H2);
//		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.ORACLE);
//		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.MYSQL);
		
		Objects.requireNonNull(this.emf);

		// -- 2 ------------
		this.em = this.emf.createEntityManager();
		assertNotNull(this.em);
		
		this.em.setFlushMode(FlushModeType.COMMIT);
	} // beforeAll
	
	@AfterAll
	void afterAll() {	// 1회성 후처리
		log.trace("afterAll() invoked.");
		
		if(this.em != null) this.em.clear();
		
		try { this.em.close(); } catch(Exception _ignored) {}
		try { this.emf.close();} catch(Exception _ignored) {}
	} // afterAll
	
	
//	@Disabled
	@Order(1)
	@Test
//	@RepeatedTest(1)
	@DisplayName("1. prepareData")
	@Timeout(value=1L, unit = TimeUnit.MINUTES)
	void prepareData() {
		log.trace("prepareData() invoked.");
		
		// -- 1 ------------------
		// 고객(Shopper4) 7명 생성
		
		IntStream.rangeClosed(1, 7).forEachOrdered(seq -> {			
			try {
				this.em.getTransaction().begin();
				
				Shopper4 transientShopper = new Shopper4();
				transientShopper.setName("NAME-"+seq);
				
				this.em.persist(transientShopper);
				
				this.em.getTransaction().commit();
			} catch(Exception e) {
				this.em.getTransaction().rollback();
				
				throw e;
			} // try-catch
		});	// .forEachOrdered

		
		// -- 2 ------------------
		// 고객이 주문할 상품을 3개 생성 및 저장
		
		IntStream.rangeClosed(1, 3).forEachOrdered(seq -> {			
			try {
				this.em.getTransaction().begin();
				
				Product4 transientProduct = new Product4();
				transientProduct.setName("NAME-" + seq);
				
				this.em.persist(transientProduct);
				
				this.em.getTransaction().commit();
			} catch(Exception e) {
				this.em.getTransaction().rollback();
				
				throw e;
			} // try-catch
		});	// .forEachOrdered

		
		// -- 3 ------------------
		// 주문내역 30개 생성
		
		IntStream.rangeClosed(1, 30).forEachOrdered(seq -> {
			Orders transientOrder = new Orders();
			
			int shopperId = new Random().nextInt(1, 8);	// half-open
			Shopper4 foundShopper = 
				this.em.<Shopper4>find(Shopper4.class, 0L+shopperId);	// 주문고객(무작위)
			
			Objects.requireNonNull(foundShopper);
			transientOrder.setShopperFK(foundShopper);
			
			// ---------------
			
			int productId = new Random().nextInt(1, 4);	// half-open
			Product4 foundProduct = 
				this.em.find(Product4.class, 0L+productId);	// 주문상품(무작위)
			
			Objects.requireNonNull(foundProduct);
			transientOrder.setProductFK(foundProduct);
			
			// ---------------
			
			transientOrder.setOrderAmount(new Random().nextInt(101));		// 주문수량(무작위)
			transientOrder.setOrderPrice(new Random().nextInt(100001));		// 주문가격(무작위)
			
			try {
				this.em.getTransaction().begin();
				
				// 이 방법은 T1과 T2에 당연히 있어야할 Children(List Collection)이
				// 없을 때에, 개발자가 직접 주문내역을 만들어 넣었던 방법입니다.
//				this.em.persist(transientOrder);		// 주문내역 저장
				
				// 하지만, 이제 T1/T2에 관계대응수(즉, Many)에 맞게 Children이
				// 추가되었고, 그에 따른 유효성 검증 주문메소드도 있기 때문에,
				// 실제 서비스의 로직대로, 고객(Shopper4)이 어떤 상품(Product4)를
				// 주문하는 순간에, 실제 주문내역 데이터가 발생함으로,
				// 주문내역 생성은 위와 같이 하시면 안되고, 주문메소드를 통해서
				// 하셔야 합니다. 즉, 아래와 같이:
				foundShopper.order(transientOrder);
				
				this.em.getTransaction().commit();
			} catch(Exception e) {
				this.em.getTransaction().rollback();
				throw e;
			} // try-catch
		}); // .forEachOrdered
		
		
		log.info("\t+ Done.");
	} // prepareData
	
	
//	@Disabled
	@Order(2)
	@Test
//	@RepeatedTest(1)
	@DisplayName("2. testObjectGraphTraverseFromShopper4ToOrders")
	@Timeout(value=1L, unit = TimeUnit.MINUTES)
	void testObjectGraphTraverseFromShopper4ToOrders() {	// Traverse : Shopper4 -> Orders (1 : N)
		log.trace("testObjectGraphTraverseFromShopper4ToOrders() invoked.");
		
		int shopperId = new Random().nextInt(1, 8);	// half-open
		Shopper4 foundShopper = this.em.<Shopper4>find(Shopper4.class, 0L+shopperId);
		
		assert foundShopper != null;
		log.info("\t+ foundShopper: {}", foundShopper);
				
		foundShopper.getMyOrders().forEach(o -> log.info(o.toString()));
	} // testObjectGraphTraverseFromShopper4ToOrders
	
	
//	@Disabled
	@Order(3)
	@Test
//	@RepeatedTest(1)
	@DisplayName("3. testObjectGraphTraverseFromProduct4ToOrders")
	@Timeout(value=1L, unit = TimeUnit.MINUTES)
	void testObjectGraphTraverseFromProduct4ToOrders() {	// Traverse : Product4 -> Orders (1 : M)
		log.trace("testObjectGraphTraverseFromProduct4ToOrders() invoked.");
		
		int productId = new Random().nextInt(1, 4);	// half-open
		Product4 foundProduct = this.em.<Product4>find(Product4.class, 0L+productId);
		
		assert foundProduct != null;
		log.info("\t+ foundProduct: {}", foundProduct);
				
		foundProduct.getMyOrders().forEach(o -> log.info(o.toString()));
	} // testObjectGraphTraverseFromProduct4ToOrders
	
	
//	@Disabled
	@Order(4)
	@Test
//	@RepeatedTest(1)
	@DisplayName("4. testObjectGraphTraverseOfOrders")
	@Timeout(value=1L, unit = TimeUnit.MINUTES)
	void testObjectGraphTraverseOfOrders() {	// Traverse : Orders
		log.trace("testObjectGraphTraverseOfOrders() invoked.");
		
		int orderId = new Random().nextInt(1, 31);	// half-open
		Orders foundOrder = this.em.<Orders>find(Orders.class, 0L+orderId);
		
		assert foundOrder != null;
		log.info("\t+ foundOrder: {}", foundOrder);
				
		// 이 이상의 정보를 알 수는 없다!!! 왜? 
		// Children 이 Shopper4 Entity 에 없기 때문에...
	} // testObjectGraphTraverseOfOrders
	
	
	

} // end class
