import { ProjectModel } from '../project/project-model';
import { SutModel } from '../sut/sut-model';
import { TJobExecModel } from './tjobExec-model';

export class TJobModel {
    id: number;
    name: string;
    imageName: string;
    sut: SutModel;
    project: ProjectModel;
    tjobExecs: TJobExecModel[];

    constructor() {
        this.id = 0;
        this.name = '';
        this.imageName = '';
        this.sut = undefined;
        this.project = undefined;
        this.tjobExecs = [];
    }
}