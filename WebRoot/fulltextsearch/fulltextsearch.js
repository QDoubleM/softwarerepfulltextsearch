(function queryInput(){	
	function enterBind() {
		$('input:eq(0)').on('keypress', function(event) {
		    if (event.keyCode == 13) {
		       var searchrange = getSearchRange();
		       search("");
		    }
		});
	}
	
	function search(searchrange){
		if($('input:eq(0)').val()!=""){
			 $.post('FullTextSearchMaster/fullTextTretrieval',{searchcon:$('input:eq(0)').val(),range:searchrange},function(data){
					var dataObj = eval("("+data+")");
					$("#result").empty();
					$.each(dataObj, function(i,n) {
						$("#result").append("<div id="+"content_left"+"><h3 class="+"t c-title-en"+"><span> <em>"+dataObj[i].fileName+"</em></span></h3>" +
								"<span class="+"c-abstract c-abstract-en"+">"+dataObj[i].filePath+"</span><br>" +
										"<span class="+"c-abstract c-abstract-en"+">"+dataObj[i].fileContent+"</span></div>"
						     );
					    });
				    });
		}	
	}
	
	function getSearchRange(){
		
	    $("#searchscope").on("click",".dropdown-toggle",function(obj){
	    	var scope = $(this).text();
	    	return scope;
		});
        //console.log("scope:"+scope);
	    //return range;
	}
	//选择表名显示查询的条件
	function getTableColumn(){
		$("#tables").on("click",".tablename",function(){	
			var table = $(this).attr("name");
			$("#queryCondition").append("<div id="+table+"><span>"+table+"</span><br></div>");
			$.post('FullTextSearchMaster/tableQueryFields',{targetTable:table},function(data){
					var dataObj = eval("("+data+")");
					$.each(dataObj, function(i,n) {
						$("#"+table+"").append("<span>"+dataObj[i].text+":" +"<input class='class_input' text='"+dataObj[i].name+"' type='text'/>"+"</span><br>"
						     );
					    });
					$("#"+table).append("<input class='dbsearch' value='搜索' type='button' /><input class='remove' value='关闭' type='button'/><br>");
				 });		  
		});
	}
	
	function removeTableOption(){
		$("#queryCondition").on("click",".remove",function(){
			$(this).parent().remove();
			if($("#queryCondition").html() == ""){
				$("#queryCondition").hide();
			}
		});
	}

	function dbQuery(){
		//搜索内容格式为：columnname:content；columnname:content;......
		$("#queryCondition").on("click",".dbsearch",function(){
			//遍历查询条件div中所有span标签
			var searchContent = "";
			$.each($("#queryCondition div"),function(){
				$(this).find("span").each(function(){
					if($(this).children().length == 0){
						searchContent = searchContent + $(this).text() + " ";
					}else{
						if($(this).children().val() != ""){
							searchContent = searchContent + $(this).text() + $(this).children().val() +";";
						}
					}
				});
				searchContent = searchContent + "|";
				
			});
			$.post('FullTextSearchMaster/dbSearcher',{searchcon:searchContent},function(data){				
				$("#result").empty();
				var dataObj = eval("("+data+")");
				$.each(dataObj, function(i,n) {
					$("#result").append("<div id="+"content_left"+"><h3 class="+"t c-title-en"+"><span> <em>"+dataObj[i].fileName+"</em></span></h3>" +
							"<span class="+"c-abstract c-abstract-en"+">"+dataObj[i].filePath+"</span><br>" +
									"<span class="+"c-abstract c-abstract-en"+">"+dataObj[i].fileContent+"</span></div>"
					     );
				    });
			    });
		});
	}	
	
	function init(){
		$('input:eq(0)').focus();
		$('input:eq(0)').val("");
		$('input:last').on('click',function(){
			search();
		});
		enterBind();
		getTableColumn();
		removeTableOption();
		getSearchRange();
		dbQuery();
		$.get('FullTextSearchMaster/getTableNames',function(data){
			var dataObj = eval("("+data.toString()+")");
			$("#tables").empty();
			$.each(dataObj, function(i,n) {
				$("#tables").append("<span class="+"tablename"+" name='"+dataObj[i]+"'>"+dataObj[i]+"</span>");
			});
		});
	}	
	$(document).ready(function(){
		init();	
	});
})();