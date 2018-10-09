'use strict';

(function () {
    var MODULE = angular.module('le', ['ngRoute']);

    MODULE.controller('ViewController', ['$scope', '$routeParams', 'ViewService',
        function($scope, $routeParams, ViewService) {
            $scope.query = {};
            $scope.clusters = [];

            var rawQuery = $routeParams.query;
            var exampleQuery = 'H2U';
            console.log(rawQuery);
            var query = rawQuery ? rawQuery : exampleQuery;
            console.log('fetching ' + query);

            ViewService.handleLigandQuery(query).then(function(response) {
                $scope.query = response.data;
                console.log($scope.query);

                $scope.query.ligands.forEach(function(ligand) {
                    ViewService.handleStructureQuery(ligand.id, ligand.pdbIds.join('')).then(function(response) {
                        response.data.forEach(function(cluster) {
                            $scope.clusters.push({
                                id : cluster.id,
                                structureIdentifiers : cluster.structureIdentifiers,
                                pdbRepresentation : cluster.pdbRepresentation
                            })
                        }, function(response) {
                            console.log(response);
                        })
                    })
                });

                console.log($scope.clusters);
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
                    component.addRepresentation('ball+stick', {
                        multipleBond: "symmetric",
                        colorValue: "grey",
                        aspectRatio: 1.2,
                        radiusScale: 2.5
                    });
                    component.autoView();
                });
        }, 0);
    }]);

    MODULE.controller('ClusterController', ['$scope', '$timeout', function($scope, $timeout) {
        $timeout(function() {
            console.log($scope.cluster);
            var stage = new NGL.Stage('ngl-cluster-' + $scope.cluster.id, { backgroundColor : '#1a1b20' });
            var stringBlob = new Blob([$scope.cluster.pdbRepresentation], { type : 'text/plain'});

            stage.loadFile(stringBlob, { ext : 'pdb'})
                .then(function (component) {
                    component.addRepresentation("ball+stick", {
                        multipleBond: "symmetric",
                        colorValue: "grey",
                        aspectRatio: 1.2,
                        radiusScale: 2.5
                    });
                    component.autoView();
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
                    return $http.get('/api/clusters/' + ligand + '/' + pdbId);
                }
            }
        }]);
})();