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
		if($('input:eq(0)').val()==""){
			return;
		}
		else{
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
	}
	function getSearchRange(){
		return $("a:eq(1)").text();
	}
	//选择表名显示查询的条件
	function getTableColumn(){
		$("#tables").on("click",".tablename",{obj:this},function(){	
			var table = $(this).attr("name");
			$("#queryCondition").append("<div id="+table+"><span>"+table+"</span></div>");
			$.post('FullTextSearch/tableQueryFields',{targetTable:table},function(data){
					var dataObj = eval("("+data+")");
					$.each(dataObj, function(i,n) {
						$("#"+table+"").append("<span>"+dataObj[i].text+":"+"</span>" +
								"<input class='class_input' text='"+dataObj[i].name+"' type='text'/><br>"
						     );
					    });
					$("#"+table).append("<input value='搜索' type='button' onclick='searchs()'/><br>");
				 });		  
		});
	}
	//获取输入框的内容以及所属字段
	
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

/*function searchs(){
	var trList = $("#history_income_list").children("tr")
	  for (var i=0;i<trList.length;i++) {
	    var tdArr = trList.eq(i).find("td");
	    var history_income_type = tdArr.eq(0).find("input").val();//收入类别
	    var history_income_money = tdArr.eq(1).find("input").val();//收入金额
	    var history_income_remark = tdArr.eq(2).find("input").val();//  备注
	    
	    alert(history_income_type);
	    alert(history_income_money);
	    alert(history_income_remark);
	  }
	alert($("#queryCondition").find("input").attr("text"))
	alert($("#queryCondition").find("input").val())
}*/