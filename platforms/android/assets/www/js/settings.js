for(var j = 0; j < group.length; j++){
	$(".filter_group_setting").append('<label for="radio-choice'+j+'"><input type="radio" name="radio-choice-group-set" id="radio-choice'+j+'" value="'+j+'"><img src="'+group[j]+'" width="32" height="32"></label>');
}

$("input[name='radio-choice-group-set']").change(function() {
	if(!_.isUndefined($("input[name='radio-choice-group-set']:checked").val())){
		choise_group = parseInt($("input[name='radio-choice-group-set']:checked").val());
		$(".new img").remove();
		$(".new").append('<img class = "barUp" src="'+group[choise_group]+'">');
		$("input[name='radio-choice-group-map']").prop("checked",true).checkboxradio("refresh");
		$('#radio-choice'+choise_group+'').prop("checked", true); 
		$("input[name='radio-choice-group-map']").prop("checked",false).checkboxradio("refresh");
		$("input[name='radio-choice-group-map'][value='"+choise_group+"']").prop("checked",true).checkboxradio("refresh");
		$("input[name='radio-choice-group']").prop("checked",false).checkboxradio("refresh");
		$("input[name='radio-choice-group'][value='"+choise_group+"']").prop("checked",true).checkboxradio("refresh");
	}
});