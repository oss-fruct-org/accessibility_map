var markers_color = [
	'img/marker/marker-blue.png',
	'img/marker/marker-red.png',
	'img/marker/marker-green.png',
	'img/marker/marker-yellow.png',
	'img/marker/marker-grey.png'
];

var word_access = [
	'трудно доступен',
	'полностью доступен',
	'условно доступен',
	'нет информации'
];

for(var j = 0; j < group.length; j++){
	$(".filter_group_map").append('<label for="radio-choice'+j+'"><input type="radio" name="radio-choice-group-map" id="radio-choice'+j+'" value="'+j+'"><img src="'+group[j]+'" width="32" height="32"></label>');
}

function resetFilterMap(){
	$("input[name='radio-choice-group-map']").prop("checked",false).checkboxradio("refresh");
	$("input[name='radio-choice-access-map']").prop("checked",false).checkboxradio("refresh");
	// Вернуть начальное отображение меток на карте
	for (var i = 0; i < markers.length; i++){
		markers[i][3] = 0;
	}
}

var markers = [
       [ 34.315425, 61.8179, 1, 0],
       [ 34.316799, 61.819483, 2, 0],
       [ 34.310018, 61.814773, 3, 0],
	   [ 34.3071, 61.813758, 4, 0],
       [ 34.306928, 61.811891, 5, 0],
       [ 34.362546,61.775106, 6, 0],
	   [ 34.308473, 61.806449, 7, 0],
       [ 34.325897, 61.793816, 8, 0],
       [ 34.324008, 61.793004, 9, 0],
	   [ 34.323064, 61.791866, 10, 0],
       [ 34.334051, 61.791886, 11, 0],
       [ 34.335338, 61.793207, 12, 0],
	   [ 34.335681, 61.788615, 13, 0],
       [ 34.344822, 61.784328, 14, 0],
       [ 34.352504, 61.786157, 15, 0],
	   [ 34.364306, 61.791439, 16, 0],
       [ 34.354822, 61.794791, 17, 0],
       [ 34.366967, 61.792567, 18, 0],
	   [ 34.379197, 61.787559, 19, 0],
       [ 34.384605, 61.787457, 20, 0],
       [ 34.36731, 61.782601, 21, 0],
	   [ 34.373447, 61.783739, 22, 0],
       [ 34.377395, 61.784958, 23, 0],
       [ 34.375593, 61.78573, 24, 0],
	   [ 34.371086, 61.785507, 25, 0],
       [ 34.369456, 61.762171, 26, 0],
       [ 34.403981, 61.761866, 27, 0],
	   [ 34.402565, 61.774197, 28, 0],
       [ 34.401964, 61.773282, 29, 0],
       [ 34.404432, 61.775365, 30, 0],
	   [ 34.415375, 61.777662, 31, 0],
       [ 34.41147, 61.7788, 32, 0],
       [ 34.405848, 61.780081, 33, 0],
	   [ 34.435138, 61.765892, 34, 0],
       [ 34.418486, 61.752418, 35, 0],
       [ 34.299032, 61.762699, 36, 0],
	   [ 34.376687, 61.786929, 37, 0],
       [ 34.341968, 61.803789, 38, 0],
       [ 34.298323, 61.783596, 39, 0],
	   [ 34.377717, 61.788046, 40, 0]
    ];
	
var coordinate1 = 34.36;
var coordinate2 = 61.78766;
var zum = 12;

$mapheight = $(window).height() - (100 + $('[data-role=header]').height() + $('[data-role=footer]').height());
$mapwidth = $(window).width()+20;
$('#map').css({'height' : $mapheight, 'width' : $mapwidth});

map = new OpenLayers.Map("map");//инициализация карты
map.addLayer(new OpenLayers.Layer.OSM());//добавление слоя

epsg4326 =  new OpenLayers.Projection("EPSG:4326"); //WGS 1984 projection
projectTo = map.getProjectionObject(); //The map projection (Spherical Mercator)

$("#OSMap").on( 'pageshow', function( ) { 
map.setCenter(new OpenLayers.LonLat(coordinate1,coordinate2) //(широта, долгота)
	.transform(
		epsg4326, // переобразование в WGS 1984
		projectTo // переобразование проекции
	), zum // масштаб
);

var vectorLayer = new OpenLayers.Layer.Vector("Overlay",
				{
                    styleMap: new OpenLayers.StyleMap({
                        graphicZIndex: 11
                    }),
                    isBaseLayer: false,
                    rendererOptions: {yOrdering: true}
                }
);

for (var i=0; i<markers.length; i++) {      
    var lon = markers[i][0];
    var lat = markers[i][1];
	var num = markers[i][2];
    var feature = new OpenLayers.Feature.Vector(
                  new OpenLayers.Geometry.Point( lon, lat ).transform(epsg4326, projectTo),
                  {description: '<a href="#info" onclick="showInformation(this.id, array_of_objects)" id = "'+num+'" style="color: #333; text-decoration: none;">'+array_of_objects[num-1][0].name+'</a><hr><p>Информация об объекте</p>'},
				  {externalGraphic: markers_color[markers[i][3]], graphicHeight: 36, graphicWidth: 23, graphicXOffset:-12, graphicYOffset:-25  }
				  	
	);             
    vectorLayer.addFeatures(feature);
}                        
map.addLayer(vectorLayer);

$('input:radio').change(function() {
	// Выбор группы - отображение метоток по цвету доступности группы
	if(!_.isUndefined($("input[name='radio-choice-group-map']:checked").val())&&_.isUndefined($("input[name='radio-choice-access-map']:checked").val())){
		var choise_group = parseInt($("input[name='radio-choice-group-map']:checked").val())+1;
		for(var m = 0; m < markers.length; m++){
			for(var n = 0; n < array_of_objects.length; n++){
				if(markers[m][2] == array_of_objects[n][7].id){
					markers[m][3] = array_of_objects[n][choise_group].id_access;
					vectorLayer.features[m].style.externalGraphic = markers_color[markers[m][3]];
					vectorLayer.redraw();
				}
			}
		} 
	}
	
});
		
$('#filterMap').click(function(){
	for (var i = 0; i < markers.length; i++){
		//markers[i][3] = 0;
		vectorLayer.features[i].style.externalGraphic = markers_color[markers[i][3]];
		vectorLayer.redraw();
	}
});
		
var controls = {
    selector: new OpenLayers.Control.SelectFeature(vectorLayer, { onSelect: createPopup, onUnselect: destroyPopup })
};

function createPopup(feature) {
    feature.popup = new OpenLayers.Popup.FramedCloud("pop",
		feature.geometry.getBounds().getCenterLonLat(),
        new OpenLayers.Size(250,150),
        '<div class="markerContent">'+feature.attributes.description+'</div>',
        null,
        true,
        function() { controls['selector'].unselectAll(); }
    );
	feature.popup.autoSize = false;
    map.addPopup(feature.popup);
}

function destroyPopup(feature) {
    feature.popup.destroy();
    feature.popup = null;
}
    
map.addControl(controls['selector']);
controls['selector'].activate();

});

function setCenterObject(number){
	
	var i = 0;
	for(i = 0; i < markers.length; i++){
		if(markers[i][2] == number){
			coordinate1 =  markers[i][0];
			coordinate2 =  markers[i][1];
			zum = 16;
		}
	}
}

