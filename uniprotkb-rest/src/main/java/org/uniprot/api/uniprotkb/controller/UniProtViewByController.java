package org.uniprot.api.uniprotkb.controller;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.uniprot.api.uniprotkb.view.ViewBy;
import org.uniprot.api.uniprotkb.view.service.UniProtViewByECService;
import org.uniprot.api.uniprotkb.view.service.UniProtViewByGoService;
import org.uniprot.api.uniprotkb.view.service.UniProtViewByKeywordService;
import org.uniprot.api.uniprotkb.view.service.UniProtViewByPathwayService;
import org.uniprot.api.uniprotkb.view.service.UniProtViewByTaxonomyService;

import io.swagger.annotations.Api;

@RestController
@Api(tags = {"uniprotkb/view"})
@RequestMapping("/uniprotkb/view")
public class UniProtViewByController {
	
	 private final UniProtViewByECService viewByECService;
	 private final UniProtViewByKeywordService viewByKeywordService;
	 private final UniProtViewByPathwayService viewByPathwayService;
	 private final UniProtViewByGoService viewByGoService;
	 private final UniProtViewByTaxonomyService viewByTaxonomyService;
	    @Autowired
	    public UniProtViewByController(UniProtViewByECService viewByECService,
	    		UniProtViewByKeywordService viewByKeywordService,
	    		 UniProtViewByPathwayService viewByPathwayService,
	    		 UniProtViewByGoService viewByGoService,
	    		 UniProtViewByTaxonomyService viewByTaxonomyService
	    		) {
	        this.viewByECService = viewByECService;
	        this.viewByKeywordService =viewByKeywordService;
	        this.viewByPathwayService = viewByPathwayService;
	        this.viewByGoService = viewByGoService;
	        this.viewByTaxonomyService = viewByTaxonomyService;
	    }
	@GetMapping(value = "/ec", produces = {APPLICATION_JSON_VALUE})
	public ResponseEntity<List<ViewBy> > getEC(
			  @RequestParam(value = "query", required = true) String query, 
			  @RequestParam(value = "parent", required = false) String parent){
		List<ViewBy> viewBys = viewByECService.get(query, parent);
		return  new ResponseEntity<> (viewBys,  HttpStatus.OK);
	}
	
	@GetMapping(value = "/keyword", produces = {APPLICATION_JSON_VALUE})
	public ResponseEntity<List<ViewBy> > getKeyword(
			  @RequestParam(value = "query", required = true) String query, 
			  @RequestParam(value = "parent", required = false) String parent){	
		return  new ResponseEntity<> (viewByKeywordService.get(query, parent),  HttpStatus.OK);
	}
	@GetMapping(value = "/pathway", produces = {APPLICATION_JSON_VALUE})
	public  ResponseEntity<List<ViewBy> > getPathway(
			  @RequestParam(value = "query", required = true) String query, 
			  @RequestParam(value = "parent", required = false) String parent){
		return  new ResponseEntity<> (viewByPathwayService.get(query, parent),  HttpStatus.OK);
	}
	
	@GetMapping(value = "/go", produces = {APPLICATION_JSON_VALUE})
	public  ResponseEntity<List<ViewBy> > getGo(
			  @RequestParam(value = "query", required = true) String query, 
			  @RequestParam(value = "parent", required = false) String parent){
		return  new ResponseEntity<> (viewByGoService.get(query, parent),  HttpStatus.OK);
	}
	@GetMapping(value = "/taxonomy", produces = {APPLICATION_JSON_VALUE})
	public ResponseEntity<List<ViewBy> > getTaxonomy(
			  @RequestParam(value = "query", required = true) String query, 
			  @RequestParam(value = "parent", required = false) String parent){
		return  new ResponseEntity<> (viewByTaxonomyService.get(query, parent),  HttpStatus.OK);
	}
}