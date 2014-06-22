function showInformation(number, array){
	var i = 0;
	var selected_object = []; 
	for(i = 0; i < array.length; i++){
		if(number == array[i][7].id){
			selected_object = array[i];
		}
	}
	
	$("div.show_information").remove();
	
	$("div#info section.list_info").append('<div class="show_information">'+
												'<h3 class="ui-bar ui-bar-a" style="text-align: center">'+selected_object[0].name+'<p></p></h3>'+
												'<h4>Категория: '+selected_object[6].category+'</h4>'+
	                                            '<h4>Адрес: </h4>'+
												'<h4>e-mail: </h4>'+
												'<h4>Телефон:<a href="tel:+(55555)">55555</a></h4>'+
											'</div>');

	for(var g = 1; g <= group.length; g++){
		$("div.show_information h3 p").append('<a class="ui-btn ui-btn-inline ui-btn-a" style="height:32px;width:32px;text-align:center">'+
												'<div style="position:relative;">'+
													'<img class="up" src="'+group[g-1]+'">'+
													'<img class="down" src="'+color[selected_object[g].id_access-1]+'">'+
												'</div></a>');
	}
}
