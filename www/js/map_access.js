var catalog = [
	['img/environment.png','Все сферы'],
	['img/hospital.png','Здравохранение'],
	['img/graduation_hat.png','Образование'],
	['img/People.png','Социальная защита'],
	['img/dumbbell.png','Физическая культура'],
	['img/IonicTemple.png','Культура'],
	['img/information2.png','Связь и информация'],
	['img/tram.png','Транспорт'],
	['img/houses.png','Жилой фонд'],
	['img/shopping_cart.png','Потребительский рынок'],
	['img/user_headset.png','Сфера услуг'],
	['img/environment_add.png','Новая категория']
];
	
for(var k = 0; k < catalog.length; k++) {
	$(".category").append('<div class="fittext1 col"><a align="bottom" href="#list" onClick="chouseCategory(array_of_objects)"><img src="'+catalog[k][0]+'"><p>'+catalog[k][1]+'</p></a></div>');
}

$("div.col")
  .mouseup(function() {
    $( this).css({'background-color' : '', 'border' : '',  'outline' :'',  'outline-offset' :''});
  })
  .mousedown(function() {
    $( this).css({'background-color' : '#C0C0C0', 'outline' : '2px solid #000000', 'outline-offset' : '-3px'});
  });

$(".fittext2").fitText();
$(".fittext1").fitText(1, { minFontSize: '15px', maxFontSize: '30px' });
