PREFIX :        <http://example/>

DELETE {}
INSERT {
}
WHERE {} 

; 

DELETE WHERE {}
;
INSERT DATA {
  :s :p :o 
  GRAPH :g { :s1 :p1 :o1 }
  :s2 :p2 :o2 
  GRAPH :g { :s3 :p3 :o3 }
}
## PREFIX :        <http://example/>
## PREFIX rdf:  	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
## PREFIX rdfs:  	<http://www.w3.org/2000/01/rdf-schema#>
## 
## ## CREATE SILENT GRAPH <http://example/graph1> ;
## ## CREATE SILENT GRAPH :graph2 ;
## 
## INSERT DATA { GRAPH :graph1 { :s :p :o } } ;
## 
## WITH :graph2
## INSERT { ?s ?p ?o }
## WHERE { GRAPH :graph1 { ?s ?p ?o } } ;
## 
## PREFIX :        <http://example/theother/>
## INSERT DATA { GRAPH :graph1 { :s1 :p1 :o1 } } ;
## 
## DROP GRAPH :graphZZZ ;