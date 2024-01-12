import React, { useCallback, useEffect, useState } from 'react';
import ReactFlow, {
    addEdge,
    FitViewOptions,
    applyNodeChanges,
    applyEdgeChanges,
    Node,
    Edge,
    NodeChange,
    EdgeChange,
    Connection,
    DefaultEdgeOptions,
    NodeTypes,
    OnNodesChange,
    OnEdgesChange,
    OnConnect,
    MarkerType,
} from 'reactflow';
import 'reactflow/dist/style.css';
import axios from 'axios';
import { CourseNode } from './CourseNode';

const BASE_URL = "http://localhost:8080/";

async function generateNodesAndEdges(): Promise<[any[], any[]]> {
    try {
        const response = await axios.get(`${BASE_URL}courses`, {withCredentials: true});

        const initialNodes: any[] = [];
        const initialEdges: any[] = [];

        const yCounters = new Map();
        let edgeId = 0;

        response.data.forEach((course: any) => {
            if (yCounters.has(course.level)) {
                yCounters.set(course.level, yCounters.get(course.level) + 150);
            } else {
                yCounters.set(course.level, 0);
            }

            const newCourseNode = {
                id: course.offeringName,
                type: 'custom',
                position: {
                    x: course.level * 500,
                    y: yCounters.get(course.level)
                },
                data: {
                    title: course.title,
                    offeringName: course.offeringName,
                    description: "",
                    prerequisiteString: "",
                    prerequisiteList: "",
                    level: course.level,
                    connectingTo: course.connectingTo
                }
            };

            

            initialNodes.push(newCourseNode);
            console.log(`Created ${course.title} node`);

            for (var courseSource of course.connectingTo) {
                const newEdge = {
                    id: edgeId, 
                    source: courseSource, 
                    target: course.offeringName, 
                    markerEnd: {
                        type: MarkerType.Arrow,
                    }, 
                }

                initialEdges.push(newEdge);
                edgeId++;
            }
     
        });

        console.log(initialNodes);
        return [initialNodes, initialEdges];
    } catch (error) {
        console.log("ERROR: " + error);
        throw error;
    } finally {
        console.log("CONFIRMED COMPLETED");
    }

}

const fitViewOptions: FitViewOptions = {
    padding: 0.2,
};    
   
const defaultEdgeOptions: DefaultEdgeOptions = {
    animated: true,
    style: { stroke: '#555', strokeWidth: 4, transition: 'stroke 0.3s ease-out', }
};

const nodeTypes: NodeTypes = {
    custom: CourseNode,
};

const Graph = () => {
    
    console.log("Graph.tsx");

    const [nodes, setNodes] = useState<Node[]>();
    const [edges, setEdges] = useState<Edge[]>();

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [fetchedNodes, fetchedEdges] = await generateNodesAndEdges();
                setNodes(fetchedNodes);
                setEdges(fetchedEdges);
            } catch (error) {
                console.log("Error fetching data:", error);
            }
        };

        fetchData();
    }, []); // Empty dependency array to run the effect only once

    const onNodesChange: OnNodesChange = useCallback(
        (changes) => setNodes((nds) => applyNodeChanges(changes, nds)),
        [setNodes]
    );

    const onEdgesChange: OnEdgesChange = useCallback(
        (changes) => setEdges((eds) => applyEdgeChanges(changes, eds)),
        [setEdges],
    );

    const onConnect: OnConnect = useCallback(
        (connection) => setEdges((eds) => addEdge(connection, eds)),
        [setEdges],
    );

    return (
        <div style={{width: '100vw', height: '100vh'}}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                fitView
                fitViewOptions={fitViewOptions}
                defaultEdgeOptions={defaultEdgeOptions}
                nodeTypes={nodeTypes}
            />
        </div>
        
    );

} 

export default Graph;