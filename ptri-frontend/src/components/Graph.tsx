import React, { useCallback, useEffect } from 'react';
import ReactFlow, { useNodesState, useEdgesState, addEdge, Connection, Edge } from 'reactflow';
import 'reactflow/dist/style.css';
import axios from 'axios';

const BASE_URL = "http://localhost:8080/";

type CourseNode = {
    id: string;
    position: {
        x: number;
        y: number;
    };
    data: {
        label: string;
        offeringName: string;
    };
}

type CourseEdge = {
    id: string;
    source: string;
    target: string;
}

async function generateNodesAndEdges(): Promise<[any[], any[]]> {
    try {
        const response = await axios.get(`${BASE_URL}courses`, {withCredentials: true});

        const initialNodes: any[] = [];
        const initialEdges: any[] = [];

        let yCounter = 0;

        const yCounters = new Map();

        response.data.forEach((course: any) => {
            if (yCounters.has(course.level)) {
                yCounters.set(course.level, yCounters.get(course.level) + 100);
            } else {
                yCounters.set(course.level, 0);
            }

            const newCourseNode = {
                id: course.offeringName,
                position: {
                    x: course.level * 200,
                    y: yCounters.get(course.level)
                },
                data: {
                    label: course.title
                }
            };
            initialNodes.push(newCourseNode);
            console.log(`Created ${course.title} node`);
            yCounter += 100;
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

const Graph = () => {
    
    console.log("Graph.tsx");

    const [nodes, setNodes, onNodesChange] = useNodesState([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);

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

    const onConnect = useCallback(
        (params: Edge | Connection) => setEdges((eds) => addEdge(params, eds)),
        [setEdges],
    );

    return (
        <div style={{ width: '100vw', height: '100vh' }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
          />
        </div>
      );

} 

export default Graph;