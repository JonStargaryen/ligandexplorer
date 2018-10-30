'use strict';

(function () {
    var MODULE = angular.module('le', ['ngRoute']);

    MODULE.controller('ViewController', ['$scope', '$routeParams', 'ViewService',
        function($scope, $routeParams, ViewService) {
            $scope.query = {};
            $scope.clusters = [];
            $scope.viewers = [];

            var rawQuery = $routeParams.query;
            // var exampleQuery = 'EST';
            // var exampleQuery = 'H2U';
            var exampleQuery = 'AMO';
            // var exampleQuery = 'ATP';
            console.log(rawQuery);
            var query = rawQuery ? rawQuery : exampleQuery;
            console.log('fetching ' + query);

            // resolve ligand query
            ViewService.handleLigandQuery(query).then(function(response) {
                $scope.query = response.data;
                console.log($scope.query);

                $scope.query.ligands.forEach(function(ligand) {
                    // cluster binding sites
                    ViewService.handleStructureQuery(ligand.id, ligand.pdbIds.join('')).then(function(response) {
                        response.data.forEach(function (cluster) {
                            console.log(cluster);

                            $scope.clusters.push({
                                id: cluster.id,
                                structureIdentifiers: cluster.structureIdentifiers,
                                pdbRepresentation: cluster.pdbRepresentation,
                                interactions: cluster.alignedInteractions
                            })
                        });
                    }, function(response) {
                        console.log(response);
                    })
                });
            }, function(response) {
                console.log(response);
            });
        }]);

    MODULE.controller('LigandController', ['$scope', '$timeout', function($scope, $timeout) {
        //TODO hide hydrogen atoms in NGL instance - how?
        $timeout(function() {
            var stage = new NGL.Stage('ngl-' + $scope.ligand.id, { backgroundColor : '#1a1b20' });
            // stage.mouseControls.add("")
            var stringBlob = new Blob([$scope.ligand.pdbRepresentation], { type : 'text/plain'});
            stage.loadFile(stringBlob, { ext : 'pdb'})
                .then(function (component) {
                    component.addRepresentation('ball+stick', {
                        // sele : "not (_H)",
                        multipleBond : "symmetric",
                        colorValue : "grey",
                        aspectRatio : 1.2,
                        radiusScale : 2.5
                    });
                    component.autoView();
                });
        }, 0);
    }]);

    MODULE.controller('ClusterController', ['$scope', '$timeout', function($scope, $timeout) {
        $timeout(function() {
            //TODO performance issues for many objects rendered (happens for alignment of 30 ligands)
            var stage = new NGL.Stage('ngl-cluster-' + $scope.cluster.id, { backgroundColor : '#1a1b20' });
            var stringBlob = new Blob([$scope.cluster.pdbRepresentation], { type : 'text/plain'});

            stage.loadFile(stringBlob, { ext : 'pdb'})
                .then(function (component) {
                    component.addRepresentation("ball+stick", {
                        // sele : "not (_H)",
                        multipleBond : "symmetric",
                        colorValue : "grey",
                        aspectRatio : 1.2,
                        radiusScale : 2.5
                    });

                    // draw spheres for interactions
                    var shape = new NGL.Shape('sphere', { disableImpostor : true });
                    $scope.cluster.interactions.forEach(function (interaction) {
                        interaction.description = interaction.interactionType + ' [' + interaction.ligandIdentifier.pdbId + '] ' +
                            interaction.ligandIdentifier.chainId + "-" + interaction.ligandIdentifier.ligandId + "-" + interaction.ligandIdentifier.residueNumber + ' <-> ' +
                            interaction.bindingSiteResidueIdentifier.chainId + "-" + interaction.bindingSiteResidueIdentifier.ligandId + "-" + interaction.bindingSiteResidueIdentifier.residueNumber;
                        console.log(interaction.description);

                        // position, color, radius, name
                        shape.addSphere(interaction.alignedCoordinates,
                            mapInteractionTypeToColor(interaction.interactionType),
                            0.75,
                            interaction.description);
                    });
                    var shapeComp = stage.addComponentFromObject(shape);
                    shapeComp.addRepresentation('buffer');

                    component.autoView();
                });
        }, 0);

        function mapInteractionTypeToColor(interactionType) {
            switch(interactionType) {
                case 'HalogenBond':
                    return new NGL.Color('#40ffbf');
                case 'HydrogenBond':
                    return new NGL.Color('#0000ff');
                case 'HydrophobicInteraction':
                    return new NGL.Color('#808080');
                case 'MetalComplex':
                    return new NGL.Color('#8c4099');
                case 'PiCationInteraction':
                    return new NGL.Color('#ff8000');
                case 'PiStackingInteraction':
                    return new NGL.Color('#00ff00');
                case 'SaltBridge':
                    return new NGL.Color('#f0c814');
                case 'WaterBridge':
                    return new NGL.Color('#bfbfff');
            }
            throw "unknown interaction type: " + interactionType;
        }
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