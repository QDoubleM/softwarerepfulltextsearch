(function queryInput(){	
	function enterBind() {
		$('input:eq(0)').on('keypress', function(event) {
		    if (event.keyCode == 13) {
		       var searchrange = getSearchRange();
		       search(searchrange);
		    }
		});
	}
	function search(searchrange){
		if($('input:eq(0)').val()!=""){
			 $.post('FullTextSearch/fullTextTretrieval',{searchcon:$('input:eq(0)').val(),range:searchrange},function(data){
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
		else
		   return;		
	}
	function getSearchRange(){
		return $("a:eq(1)").text();
	}
	//选择表名显示查询的条件
	function getTableColumn(){
		$("#tables").on("click",".tablename",{obj:this},function(){	
			var table = $(this).attr("name");
			$("#queryCondition").append("<div id="+table+"><span>"+table+"</span><br></div>");
			$.post('FullTextSearch/tableQueryFields',{targetTable:table},function(data){
					var dataObj = eval("("+data+")");
					$.each(dataObj, function(i,n) {
						$("#"+table+"").append("<span>"+dataObj[i].text+":"+"</span>" +
								"<input class='class_input' text='"+dataObj[i].name+"' type='text'/><br>"
						     );
					    });
					$("#"+table).append("<input value='搜索' type='button' onclick='searchs()'/><input class='remove' value='关闭' type='button'/><br>");
				 });		  
		});
	}
	
	function removeTableOption(){
		$("#queryCondition").on("click",".remove",function(event){
			$("#"+event.target.parentNode.id).remove();
			if($("#queryCondition").html() == ""){
				$("#queryCondition").hide();
			}
		})
	}
	
	function dbQuery(){
		//搜索内容格式为：columnname:content；columnname:content;......
		$.post('FullTextSearch/dbSearcher',{searchcon:$('input:eq(0)').val(),range:searchrange},function(data){
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
	
	
	
	function init(){
		$('input:eq(0)').focus();
		$('input:eq(0)').val("");
		$('input:last').on('click',function(){
			search();
		});
		enterBind();
		getTableColumn();
		removeTableOption();
		$.get('FullTextSearch/getTableNames',function(data){
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