var ZafiraApp = angular.module('ZafiraApp', [ 'ngRoute', 'ngSanitize', 'chieffancypants.loadingBar', 'ngAnimate', 'bw.paging', 'ui.bootstrap.modal', 'ngCookies', 'n3-line-chart', 'n3-pie-chart', 'timer' ]);

ZafiraApp.directive('ngReallyClick', [ function() {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			element.bind('click', function() {
				var message = attrs.ngReallyMessage;
				if (message && confirm(message)) {
					scope.$apply(attrs.ngReallyClick);
				}
			});
		}
	}
}]);

ZafiraApp.service('UtilService', function() {
	this.truncate = function(fullStr, strLen) {
		if (fullStr == null || fullStr.length <= strLen) return fullStr;
	    var separator = '...';
	    var sepLen = separator.length,
	        charsToShow = strLen - sepLen,
	        frontChars = Math.ceil(charsToShow/2),
	        backChars = Math.floor(charsToShow/2);
	    return fullStr.substr(0, frontChars) + 
	           separator + 
	           fullStr.substr(fullStr.length - backChars);
    };
});

ZafiraApp.factory('UserService', function($http) {
	var userService = {
			getCurrentUser : function() {
			var promise = $http.get('users/current').then(function(rs) {
				return rs.data;
			});
			return promise;
		}
	};
	return userService;
});

ZafiraApp.factory('DashboardService', function($http) {
	var dashboardService = {
			getUserPerformanceDashboardId : function() {
			var promise = $http.get('dashboards/all?type=USER_PERFORMANCE').then(function(rs) {
				if(rs.data.length > 0)
				{
					return rs.data[0].id;
				}
				else
				{
					return null;
				}
			});
			return promise;
		}
	};
	return dashboardService;
});

ZafiraApp.factory('SettingsService', function($http) {
	var settingsService = {
			getSetting : function(name) {
			var promise = $http.get('settings/' + name).then(function(rs) {
				return rs.data;
			});
			return promise;
		}
	};
	return settingsService;
});

ZafiraApp.provider('ProjectProvider', function() {
    this.$get = function($cookieStore) {
        return {
            initProject: function(sc) {
            	if($cookieStore.get("project") != null)
            	{
            		sc.project = $cookieStore.get("project");
            	}
                return sc;
            },
            getProject: function() {
            	return $cookieStore.get("project");
            },
            setProject: function(project) {
            	$cookieStore.put("project", project);
            },
            getProjectQueryParam: function(sc) {
            	var query = "";
            	if($cookieStore.get("project") != null)
            	{
            		query = "?project=" + $cookieStore.get("project").name;
            	}
                return query; 
            }
        }
    };
});

angular.module('ZafiraApp').filter('orderObjectBy', function() {
	  return function(items, field, reverse) {
	    var filtered = [];
	    angular.forEach(items, function(item) {
	      filtered.push(item);
	    });
	    filtered.sort(function (a, b) {
	      return (a[field] > b[field] ? 1 : -1);
	    });
	    if(reverse) filtered.reverse();
	    return filtered;
	  };
});

ZafiraApp.directive('showMore', [function() {
    return {
        restrict: 'AE',
        replace: true,
        scope: {
            text: '=',
            limit:'='
        },

        template: '<div class="wrap"><div ng-show="largeText"> {{ text | subString :0 :end }}.... <a href="javascript:;" ng-click="showMore()" ng-show="isShowMore">Show more</a><a href="javascript:;" ng-click="showLess()" ng-hide="isShowMore">Show less </a></div><div ng-hide="largeText">{{ text }}</div></div> ',

        link: function(scope, iElement, iAttrs) {

            
            scope.end = scope.limit;
            scope.isShowMore = true;
            scope.largeText = true;

            if (scope.text.length <= scope.limit) {
                scope.largeText = false;
            };

            scope.showMore = function() {

                scope.end = scope.text.length;
                scope.isShowMore = false;
            };

            scope.showLess = function() {

                scope.end = scope.limit;
                scope.isShowMore = true;
            };
        }
    };
}]);

ZafiraApp.filter('subString', function() {
    return function(str, start, end) {
        if (str != undefined) {
            return str.substr(start, end);
        }
    }
})

ZafiraApp.directive('contextMenu', ["$parse", "$q", "$sce", function ($parse, $q, custom, $sce) {

    var contextMenus = [];
    var $currentContextMenu = null;
    var defaultItemText = "New Item";

    var removeContextMenus = function (level) {
        /// <summary>Remove context menu.</summary>
        while (contextMenus.length && (!level || contextMenus.length > level)) {
            contextMenus.pop().remove();
        }
        if (contextMenus.length == 0 && $currentContextMenu) {
            $currentContextMenu.remove();
        }
    };


    var processTextItem = function ($scope, item, text, event, model, $promises, nestedMenu, $) {
        "use strict";

        var $a = $('<a>');
        $a.css("padding-right", "8px");
        $a.attr({ tabindex: '-1', href: '#' });

        if (typeof item[0] === 'string') {
            text = item[0];
        }
        else if (typeof item[0] === "function") {
            text = item[0].call($scope, $scope, event, model);
        } else if (typeof item.text !== "undefined") {
            text = item.text;
        }

        var $promise = $q.when(text);
        $promises.push($promise);
        $promise.then(function (text) {
            if (nestedMenu) {
                $a.css("cursor", "default");
                $a.append($('<strong style="font-family:monospace;font-weight:bold;float:right;">&gt;</strong>'));
            }
            $a.append(text);
        });

        return $a;

    };

    var processItem = function ($scope, event, model, item, $ul, $li, $promises, $q, $, level) {
        /// <summary>Process individual item</summary>
        "use strict";
        // nestedMenu is either an Array or a Promise that will return that array.
        var nestedMenu = angular.isArray(item[1]) || (item[1] && angular.isFunction(item[1].then))
          ? item[1] : angular.isArray(item[2]) || (item[2] && angular.isFunction(item[2].then))
          ? item[2] : angular.isArray(item[3]) || (item[3] && angular.isFunction(item[3].then))
          ? item[3] : null;

        // if html property is not defined, fallback to text, otherwise use default text
        // if first item in the item array is a function then invoke .call()
        // if first item is a string, then text should be the string.

        var text = defaultItemText;
        if (typeof item[0] === 'function' || typeof item[0] === 'string' || typeof item.text !== "undefined") {
            text = processTextItem($scope, item, text, event, model, $promises, nestedMenu, $);
        }
        else if (typeof item.html !== "undefined") {
            // leave styling open to dev
            text = item.html
        }

        $li.append(text);




        // if item is object, and has enabled prop invoke the prop
        // els if fallback to item[2]

        var isEnabled = function () {
            if (typeof item.enabled !== "undefined") {
                return item.enabled.call($scope, $scope, event, model, text);
            } else if (typeof item[2] === "function") {
                return item[2].call($scope, $scope, event, model, text);
            } else {
                return true;
            }
        };

        registerEnabledEvents($scope, isEnabled(), item, $ul, $li, nestedMenu, model, text, event, $, level);
    };

    var handlePromises = function ($ul, level, event, $promises) {
        /// <summary>
        /// calculate if drop down menu would go out of screen at left or bottom
        /// calculation need to be done after element has been added (and all texts are set; thus thepromises)
        /// to the DOM the get the actual height
        /// </summary>
        "use strict";
        $q.all($promises).then(function () {
            var topCoordinate = event.pageY;
            var menuHeight = angular.element($ul[0]).prop('offsetHeight');
            var winHeight = event.view.innerHeight;
            if (topCoordinate > menuHeight && winHeight - topCoordinate < menuHeight) {
                topCoordinate = event.pageY - menuHeight;
            } else if(winHeight <= menuHeight) {
                // If it really can't fit, reset the height of the menu to one that will fit
                angular.element($ul[0]).css({"height": winHeight - 5, "overflow-y": "scroll"});
                // ...then set the topCoordinate height to 0 so the menu starts from the top
                topCoordinate = 0;
            } else if(winHeight - topCoordinate < menuHeight) {
                var reduceThreshold = 5;
                if(topCoordinate < reduceThreshold) {
                    reduceThreshold = topCoordinate;
                }
                topCoordinate = winHeight - menuHeight - reduceThreshold;
            }

            var leftCoordinate = event.pageX;
            var menuWidth = angular.element($ul[0]).prop('offsetWidth');
            var winWidth = event.view.innerWidth;
            var rightPadding = 5;
            if (leftCoordinate > menuWidth && winWidth - leftCoordinate - rightPadding < menuWidth) {
                leftCoordinate = winWidth - menuWidth - rightPadding;
            } else if(winWidth - leftCoordinate < menuWidth) {
                var reduceThreshold = 5;
                if(leftCoordinate < reduceThreshold + rightPadding) {
                    reduceThreshold = leftCoordinate + rightPadding;
                }
                leftCoordinate = winWidth - menuWidth - reduceThreshold - rightPadding;
            }

            $ul.css({
                display: 'block',
                position: 'absolute',
                left: leftCoordinate + 'px',
                top: topCoordinate + 'px'
            });
        });

    };

    var registerEnabledEvents = function ($scope, enabled, item, $ul, $li, nestedMenu, model, text, event, $, level) {
        /// <summary>If item is enabled, register various mouse events.</summary>
        if (enabled) {
            var openNestedMenu = function ($event) {
                removeContextMenus(level + 1);
                /*
                 * The object here needs to be constructed and filled with data
                 * on an "as needed" basis. Copying the data from event directly
                 * or cloning the event results in unpredictable behavior.
                 */
                var ev = {
                    pageX: event.pageX + $ul[0].offsetWidth - 1,
                    pageY: $ul[0].offsetTop + $li[0].offsetTop - 3,
                    view: event.view || window
                };

                /*
                 * At this point, nestedMenu can only either be an Array or a promise.
                 * Regardless, passing them to when makes the implementation singular.
                 */
                $q.when(nestedMenu).then(function(promisedNestedMenu) {
                    renderContextMenu($scope, ev, promisedNestedMenu, model, level + 1);
                });
            };

            $li.on('click', function ($event) {
                $event.preventDefault();
                $scope.$apply(function () {
                    if (nestedMenu) {
                        openNestedMenu($event);
                    } else {
                        $(event.currentTarget).removeClass('context');
                        removeContextMenus();

                        if (angular.isFunction(item[1])) {
                            item[1].call($scope, $scope, event, model, text)
                        } else {
                            item.click.call($scope, $scope, event, model, text);
                        }
                    }
                });
            });

            $li.on('mouseover', function ($event) {
                $scope.$apply(function () {
                    if (nestedMenu) {
                        openNestedMenu($event);
                    }
                });
            });
        } else {
            $li.on('click', function ($event) {
                $event.preventDefault();
            });
            $li.addClass('disabled');
        }

    };


    var renderContextMenu = function ($scope, event, options, model, level, customClass) {
        /// <summary>Render context menu recursively.</summary>
        if (!level) { level = 0; }
        if (!$) { var $ = angular.element; }
        $(event.currentTarget).addClass('context');
        var $contextMenu = $('<div>');
        if ($currentContextMenu) {
            $contextMenu = $currentContextMenu;
        } else {
            $currentContextMenu = $contextMenu;
            $contextMenu.addClass('angular-bootstrap-contextmenu dropdown clearfix');
        }
        if (customClass) {
            $contextMenu.addClass(customClass);
        }
        var $ul = $('<ul>');
        $ul.addClass('dropdown-menu');
        $ul.attr({ 'role': 'menu' });
        $ul.css({
            display: 'block',
            position: 'absolute',
            left: event.pageX + 'px',
            top: event.pageY + 'px',
            "z-index": 10000
        });

        var $promises = [];

        angular.forEach(options, function (item) {

            var $li = $('<li>');
            if (item === null) {
                $li.addClass('divider');
            } else if (typeof item[0] === "object") {
                // nothing
            } else {
                processItem($scope, event, model, item, $ul, $li, $promises, $q, $, level);
            }
            $ul.append($li);
        });
        $contextMenu.append($ul);
        var height = Math.max(
            document.body.scrollHeight, document.documentElement.scrollHeight,
            document.body.offsetHeight, document.documentElement.offsetHeight,
            document.body.clientHeight, document.documentElement.clientHeight
        );
        $contextMenu.css({
            width: '100%',
            height: height + 'px',
            position: 'absolute',
            top: 0,
            left: 0,
            zIndex: 9999,
            "max-height" : window.innerHeight - 3,
        });
        $(document).find('body').append($contextMenu);

        handlePromises($ul, level, event, $promises);

        $contextMenu.on("mousedown", function (e) {
            if ($(e.target).hasClass('dropdown')) {
                $(event.currentTarget).removeClass('context');
                removeContextMenus();
            }
        }).on('contextmenu', function (event) {
            $(event.currentTarget).removeClass('context');
            event.preventDefault();
            removeContextMenus(level);
        });

        $scope.$on("$destroy", function () {
            removeContextMenus();
        });

        contextMenus.push($ul);
    };

    function isTouchDevice() {
      return 'ontouchstart' in window        // works on most browsers
          || navigator.maxTouchPoints;       // works on IE10/11 and Surface
    };

    return function ($scope, element, attrs) {
        var openMenuEvent = "contextmenu";
        if(attrs.contextMenuOn && typeof(attrs.contextMenuOn) === "string"){
            openMenuEvent = attrs.contextMenuOn;
        }
        element.on(openMenuEvent, function (event) {
            event.stopPropagation();
            event.preventDefault();
            
            // Don't show context menu if on touch device and element is draggable
            if(isTouchDevice() && element.attr('draggable') === 'true') {
              return false;
            }

            $scope.$apply(function () {
                var options = $scope.$eval(attrs.contextMenu);
                var customClass = attrs.contextMenuClass;
                var model = $scope.$eval(attrs.model);
                if (options instanceof Array) {
                    if (options.length === 0) { return; }
                    renderContextMenu($scope, event, options, model, undefined, customClass);
                } else {
                    throw '"' + attrs.contextMenu + '" not an array';
                }
            });
        });
    };
}]);