(function($) {
  $(function() {
    var window_width = $(window).width();

    // CSS Transitions Demo Init
    if ($('#scale-demo').length && $('#scale-demo-trigger').length) {
      $('#scale-demo-trigger').click(function() {
        $('#scale-demo').toggleClass('scale-out');
      });
    }

    // Plugin initialization
    $('.carousel').carousel();
    $('.carousel.carousel-slider').carousel({
      fullWidth: true,
      indicators: true,
      onCycleTo: function(item, dragged) {}
    });
    $('.collapsible').collapsible();
    $('.collapsible.expandable').collapsible({
      accordion: false
    });

    $('.dropdown-trigger').dropdown();
    $('.slider').slider();
    $('.parallax').parallax();
    $('.materialboxed').materialbox();
    $('.modal').modal();
    $('.scrollspy').scrollSpy();
    $('.datepicker').datepicker();
    $('.tabs').tabs();
    $('.timepicker').timepicker();
    $('.tooltipped').tooltip();
    $('select')
      .not('.disabled')
      .formSelect();
    $('.sidenav').sidenav();
    $('.tap-target').tapTarget();
    $('input.autocomplete').autocomplete({
      data: { Apple: null, Microsoft: null, Google: 'http://placehold.it/250x250' }
    });
    $('input[data-length], textarea[data-length]').characterCounter();

  }); // end of document ready
})(jQuery); // end of jQuery name space
