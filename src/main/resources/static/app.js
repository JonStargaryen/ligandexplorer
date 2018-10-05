'use strict';

(function () {
    var MODULE = angular.module('le', ['ngRoute']);

    MODULE.controller('ViewController', ['$scope', '$routeParams', 'ViewService',
        function($scope, $routeParams, ViewService) {
            $scope.query = {};
            $scope.hits = [];

            var rawQuery = $routeParams.query;
            var exampleQuery = 'H2U';
            console.log(rawQuery);
            var query = rawQuery ? rawQuery : exampleQuery;
            console.log('fetching ' + query);

            ViewService.handleLigandQuery(query).then(function(response) {
                $scope.query = response.data;
            }, function(response) {
                console.log(response);
            });
        }]);

    MODULE.controller('LigandController', ['$scope', '$timeout', function($scope, $timeout) {
        // var stage = new NGL.Stage('ngl-' + $scope.ligand.id, { backgroundColor : "white" });
        $timeout(function() {
            var stage = new NGL.Stage('ngl-' + $scope.ligand.id);
            // var stage = new NGL.Stage('viewport');
            console.log($scope.ligand.pdbRepresentation);
            var stringBlob = new Blob([$scope.ligand.pdbRepresentation], { type : 'text/plain'});
            stage.loadFile(stringBlob, { ext : 'pdb'})
            // stage.loadFile( "http://files.rcsb.org/download/5IOS.cif")
                .then(function (component) {
                    // draw actual structure
                    component.addRepresentation('ball+stick');
                    // adjust view
                    component.autoView();
                });
        }, 0);
    }]);

    MODULE.factory('ViewService', ['$http',
        function($http) {
            return {
                handleLigandQuery : function(queryString) {
                    return $http.get('/api/query/' + queryString);
                }
            }
        }]);
})();