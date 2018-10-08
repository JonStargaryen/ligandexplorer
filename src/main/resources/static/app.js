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
                console.log($scope.query);

                $scope.query.ligands.forEach(function(ligand) {
                    ligand.pdbIds.forEach(function(pdbId) {
                        ViewService.handleStructureQuery(ligand.id, pdbId).then(function(response) {
                            // flatten results: 1 query may return more than 1 hit (e.g. in distinct chains)
                            var hitContainer = response.data;
                            hitContainer.structureIdentifiers.forEach(function(si) {
                                $scope.hits.push({ id : si,
                                    name : hitContainer.name,
                                    pdbRepresentation : hitContainer.pdbRepresentation });
                            });
                        }, function(response) {
                            console.log(response);
                        });
                    })
                });
                console.log($scope.hits);
            }, function(response) {
                console.log(response);
            });
        }]);

    MODULE.controller('LigandController', ['$scope', '$timeout', function($scope, $timeout) {
        $timeout(function() {
            var stage = new NGL.Stage('ngl-' + $scope.ligand.id, { backgroundColor : '#1a1b20' });
            var stringBlob = new Blob([$scope.ligand.pdbRepresentation], { type : 'text/plain'});
            stage.loadFile(stringBlob, { ext : 'pdb'})
                .then(function (component) {
                    // draw actual structure
                    component.addRepresentation('ball+stick');
                    // adjust view
                    component.autoView();
                });
        }, 0);
    }]);

    MODULE.controller('StructureController', ['$scope', '$timeout', function($scope, $timeout) {
        $timeout(function() {
            console.log($scope.hit);
            var stage = new NGL.Stage('ngl-' + $scope.hit.id.pdbId + '-' + $scope.hit.id.ligandId + '-' + $scope.hit.id.chainId + '-' + $scope.hit.id.residueNumber, { backgroundColor : '#1a1b20' });
            var stringBlob = new Blob([$scope.hit.pdbRepresentation], { type : 'text/plain'});

            stage.loadFile(stringBlob, { ext : 'pdb'})
                .then(function (component) {
                    component.addRepresentation("ball+stick", {
                        multipleBond: "symmetric",
                        colorValue: "grey",
                        aspectRatio: 1.2,
                        radiusScale: 2.5
                    });
                    component.autoView('"' + $scope.hit.id.ligandId + '"');
                });
        }, 0);
    }]);

    MODULE.factory('ViewService', ['$http',
        function($http) {
            return {
                handleLigandQuery : function(queryString) {
                    return $http.get('/api/query/' + queryString);
                },
                handleStructureQuery : function(ligand, pdbId) {
                    return $http.get('/api/structure/' + ligand + '/' + pdbId);
                }
            }
        }]);
})();