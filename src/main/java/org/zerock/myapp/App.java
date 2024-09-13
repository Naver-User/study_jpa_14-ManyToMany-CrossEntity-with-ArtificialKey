package org.zerock.myapp;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;


//@Log4j2
@Slf4j
public class App {
	
    public static void main( String[] args ) {
        log.trace("main({}) invoked.", Arrays.toString(args));
        
        Entity1 entity1 = new Entity1();
        Entity2 entity2 = new Entity2();
        
        entity1.setEntity(entity2);	// E1 has E2
        entity2.setEntity(entity1);	// E2 has E1
        
        log.info(entity1.toString());
    } // main
    
} // end class
