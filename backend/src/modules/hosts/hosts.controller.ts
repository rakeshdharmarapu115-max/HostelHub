import { Request, Response, NextFunction } from 'express';
import { HostsService } from './hosts.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';

const hostsService = new HostsService();

export class HostsController {
  async getAllHosts(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const hosts = await hostsService.getAllHosts();
      sendSuccess(res, 'Hosts retrieved successfully', hosts);
    } catch (error) {
      next(error);
    }
  }

  async getHostById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const host = await hostsService.getHostById(req.params.id);
      sendSuccess(res, 'Host details retrieved', host);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async verifyHost(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { verified } = req.body;
      const result = await hostsService.verifyHost(req.params.id, Boolean(verified));
      sendSuccess(res, 'Host verification status updated', result);
    } catch (error) {
      next(error);
    }
  }
}
