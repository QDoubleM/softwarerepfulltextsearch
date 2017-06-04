(function queryInput(){	
	function enterBind() {
		$('input:eq(0)').on('keypress', function(event) {
		    if (event.keyCode == 13) {
		  	  search();
		    }
		});
	}
	function search(){
		if($('input:eq(0)').val()==""){
			window.location.reload();
		}
		else{
			$.post('FullTextSearch/fullTextTretrieval',{searchcon:$('input:eq(0)').val()},function(issuccess){
				window.location.href="http://qmm-pc:8080/fulltextsearch/search-result.html";
			});
		}
	}
	function getSearchRange(){
		$('.active').on('click',function(){
			alert($('.active').attr("name"));
		});
	}
	function getDBNames(){
		$(".dropdown-toggle").on('click',function(){
			$.post();
			$.get('FullTextSearch/getTableNames',function(data){
				var dataObj = eval("("+data+")");
				$.each(dataObj, function(i,n) {
					$("#tables").append("<span class="+"tablename"+">"+dataObj[i]+"</span>");
				});
			});
		});
	}
	function getRange(){
		$(".tablemame").on('click',function(){
			 var tableName=document.getElementsByClassName("classname");
			 alert(tableName);
		});
	}
	function init(){
		$('input:eq(0)').focus();
		$('input:eq(0)').attr("value","");
		$('input:last').on('click',function(){
			search();
		});
	}	
	$(document).ready(function(){
		init();
		//getSearchRange();
		enterBind();
		getDBNames();
		getRange();
	});
})();

